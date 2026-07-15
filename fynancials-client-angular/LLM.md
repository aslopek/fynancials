# LLM.md

This file provides guidance to LLM coding agents (Claude Code, etc.) when working with code in this repository.

## What this package is

The Angular + NgRx frontend, packaged as the Electron desktop app that ships to users. See the root `LLM.md` for how this fits together with
`fynancials-api` and `fynancials-server-spring`.

## Commands

- `npm run generate` — regenerate all API clients in `src/gen/api/*` from `../fynancials-api` (config in `openapitools.json` +
  `api/openapi-config.json`). Run this after any spec change; `src/gen` is otherwise untouched by hand.
- `npm run serve` — `ng serve` at `http://localhost:4200`; requires the Spring backend running separately (e.g. `mvn spring-boot:run` in
  `fynancials-server-spring`).
- `npm run build` — `clean` (dist/electron-out/gen) + `generate` + `ng build --base-href ./` + `licenses:generate`.
- `npm run watch` — incremental dev build (`ng build --watch --configuration development`), no dev server.
- `npm run electron:start` — run the packaged desktop shell locally via electron-forge (spawns the bundled backend jar from
  `resources/backend.jar`).
- `npm run electron:pack` — build an unpacked app. `forge.config.js`'s `generateAssets` hook copies
  `../fynancials-server-spring/target/fynancials-server-spring-<version>.jar` into `resources/backend.jar`, so the Spring backend must
  already be built (`mvn package`). Ignore `electron:make` script.
- `npm run licenses:generate` — collect licenses of packaged dependencies into a JSON file.
- `npm run licenses:check` — verify license compatibility of dependencies.
- `npm run test` — run all tests (config in `jest.config.ts`, uses `ts-jest`). `npx jest <path-to-spec>` runs a single file.

## Architecture

- **Electron shell**: `main.js` is the Electron main process — it locates/prompts to download Corretto 25, spawns the Spring backend jar
  as a child process, manages `fynancials.config.json` (env vars like `FY_DB_FILE_PATH`, per-file password prompts), and opens the
  `BrowserWindow` pointing at the built Angular app (`dist/fynancials/browser/index.html`).
- **Two kinds of state, kept strictly separate** — see the dedicated sections below for each:
  - The **global NgRx store** (`src/store/`) holds only data that's genuinely shared/global (loaded entities, cross-screen config) — never
    screen-local drafts or UI-only state.
  - **NgRx Signal Stores**, one per feature/dialog/wizard, colocated under that feature's own `store/` subfolder
    (e.g. `src/depot/depot-performance/store/`, `src/security/update-security/store/`), hold everything else: form drafts, wizard/tab
    progress, UI toggles, derived view state. A Signal Store can be shared by several components at once.
- **Feature folders** under `src/` (`depot`, `dividends`, `security`, `settings`) hold routed page/feature components and consume the global
  store + generated API clients directly, or a local Signal Store where one exists; `src/common` holds cross-feature building blocks
  (re-exported via `src/common/index.ts`: shared components, `fy-*` pipes for currency/date/decimal/percent formatting, the
  `ReadableSignalStore`/ `WritableSignalStore` types); `src/app` holds app-shell chrome (header incl. notifications, database connection
  dialog, splash screen, license, info).
- **Custom Pipes**: Use the aforementioned custom pipes instead of angular default pipes. In addition, there are pipes for specific purposes
  which must be used instead of accessing raw properties:
  - `country.pipe.ts`: displaying country flag emojis
  - `depot-logo-url.pipe.ts`: use in conjunction with `<img>` for displaying a depot's logo
  - `file-preview.pipe.ts`: use for displaying files (e.g. images or other content) inserted by the user. Be aware that the user may
    unknowingly insert attacker-controlled content such as malicious PDF.
  - `security-logo-url.pipe.ts`: use in conjunction with `<img>` for displaying a security's logo
  - `security-name.pipe.ts`: use to display security names. Unlike security groups, it is not allowed to access raw `security.name` property
    for this purpose.
  - `security-symbols.pipe.ts`: use to concatenate all `security.symbols` for displaying.
  - `transaction-type-display-icon.pipe.ts`: use in conjunction with `<mat-icon>` for displaying an icon for the given `TransactionType`
  - `transaction-type-display-name.pipe.ts`: use to display a human-readable name for the given `TransactionType`
- **Generated API clients** (`src/gen/api/<domain>/`): one Angular service class per spec (e.g. `DepotApi`, `ConfigApi`, `DividendApi`),
  injected directly into effects/Signal Store code — there is no handwritten HTTP layer on top (exception: GitHub update check).
- Charts use `ngx-echarts` with `echarts/core` and only the chart types/components registered in `app.config.ts` (`BarChart`, `LineChart`,
  `PieChart`, `Grid`/`Legend`/`Tooltip`, `SVGRenderer`) — add new echarts features to that `echarts.use([...])` call, not ad hoc per
  component.
- **Chart options** for echarts are always built in a dedicated custom pipe per chart (e.g. `dividend-bar-chart.pipe.ts`,
  `position-pie-chart.pipe.ts`), never inline in the component.

## Global NgRx store conventions

Treat the `security` slice (`src/store/security/`) as the canonical template for any new or extended slice:

- Top level: `<slice>.state.ts`, `<slice>.actions.ts` (one `createActionGroup`, with a named `*ActionArgs` type exported per action that
  takes props), `<slice>.reducer.ts`, `<slice>.selector.ts`.
- `effects/` subfolder — one file per action, exporting a function that takes `actions$` (+ whatever API/store deps it needs) and returns
  the effect; `<slice>.effects.ts` only wires these into `createEffect(...)` calls, it never contains effect logic itself.
- `reducers/` subfolder — one pure function per state transition (e.g. `overwrite-security.reducer.ts`), composed with `on(...)` in
  `<slice>.reducer.ts`.
- `selectors/` subfolder — one function per derived value, following **dependency inversion**: each selector file declares its own minimal
  input type as a `Pick<SliceState, '...'>` (e.g.
  `GetHistoricalSecurityPriceConfigState = Pick<SecurityState, 'historicalSecurityPriceConfigs'>`) containing only the fields it actually
  reads, and the function is typed against that, not against the full `SliceState`. `<slice>.selector.ts` then composes
  `createFeatureSelector` + `createSelector` with these functions to produce the public `MemoizedSelector<AppState, T>` exports. New
  selectors must follow this pattern (some older selectors in this codebase predate it and take the full state directly — don't copy those).
- Only put data in a slice's state if it's genuinely global (needed across unrelated components/screens or persisted/loaded once and read
  from many places). Everything else belongs in a Signal Store.
- **If two domains interact with each other, dependency inversion must be used.** A domain in this sense is a global-store slice. Say
  domain A performs an action (whether pure or with side effects) and Domain B needs to adjust after A is done. Then A does not dispatch
  one of B's actions, it rather dispatches a (`...Done` | `...Success` | `...Error`) action from its own domain. B listens to these
  actions in a dedicated `<verb>-<noun>-on.effect.ts` and reacts appropriately (see `depot/effects/reload-depots-on.effect.ts` and
  `security/effects/load-securities-on.effect.ts` for the pattern, including how to add further triggering actions). This rule is scoped
  to global-store slices only: components and Signal Stores are not domains and may dispatch any slice's actions directly.

## Signal Stores (component/feature-local state)

Use `@ngrx/signals` (`signalStore`) for state scoped to a component or a small subtree — form drafts, wizard steps, dialog/tab state,
UI-only toggles. Never put this in the global store, and never duplicate global data into a Signal Store's own state — read it live via
`globalStore.selectSignal(...)` instead.

**`depot-performance/store/` is the newest Signal Store in the codebase and the canonical reference for current architecture rules** (some
older stores, e.g. `add-security-wizard.store.ts` / `update-security.store.ts`, predate parts of this pattern — prefer the
`depot-performance` style for new work). Its file layout:

- `<name>.store.ts` — defines `<Name>State`, `<Name>Computed`, `<Name>Methods` types, `initialState`, and the store itself via
  `signalStore(withState(...), withComputed(...), withMethods(...), withHooks(...))`. Exports a
  `Readable<Name>Store = ReadableSignalStore<State, Computed, Methods>` type alias for consumers that only read state/call methods.
- `computed/<name>.ts` — one exported function per computed signal. Takes `ReadableSignalStore<State>` (and, when it needs global data, the
  injected `Store<AppState>`) as parameters and returns an explicitly-typed `Signal<T>`. This is where local state and global-store
  selectors get combined (see `computed/depot-values.ts`, which merges the store's own `dataRange`/`addCashToAbsoluteValue` signals with the
  global `depotPerformance` selector).
- `methods/<name>.ts` — one exported function per mutation, taking `WritableSignalStore<State, Computed>` plus its own args and calling
  `patchState(...)`.
- `effects/<name>.ts` — one exported function per side-effecting `rxMethod`, hooked up in `withHooks.onInit`.
- Domain-specific helper types/logic that don't fit `computed`/`methods`/`effects` get their own subfolder (see `benchmark/` in
  `depot-performance/store/`).

Always type the `withComputed`/`withMethods` factory parameters explicitly using `ReadableSignalStore<...>` / `WritableSignalStore<...>`
from `src/common/types/signal-store.type.ts` — never let them be inferred, and never pass the raw store class type around.

**One Signal Store instance can serve several components at once**: provide it once in a
container component's `providers: [XStore]`, then have descendant components
`inject(XStore)` directly (typed as `Readable<X>Store`). Examples:

- `DepotPerformanceComponent` provides `DepotPerformanceStore`; its children (`DataRangeSelectionComponent`,
  `DepotPerformanceKpisComponent`, `DepotPerformanceChartComponent`, `TransactionOverviewComponent`, `BenchmarkComponent`) each
  `inject(DepotPerformanceStore)` independently.
- The "Create Security Wizard" (`AddSecurityWizardComponent`) provides `addSecurityWizardStore` once, shared across its wizard steps.
- The Edit Security Modal (`UpdateSecurityComponent`) provides `updateSecurityStore` once, shared across its tabs.

## TypeScript conventions

- Strict typing throughout; avoid inferred types wherever practical — annotate function return types, `const` bindings holding non-trivial
  values, and store/selector/effect parameters explicitly (as in all examples above).
- When no existing type fits exactly, define a new, narrowly-scoped one (e.g. a `Get<X>State` selector input, an
  `<X>ActionArgs`/`<X>EffectArgs` type) rather than widening an existing type or leaving it inferred.
- Use `type` over `interface`. Use `interface` if and only if there is a class implementing the interface.
- One-line JSON objects and arrays are allowed if and only if they are empty or contain maximum one key / item.
  - `[]`, `['foo']` are okay, `['foo', 'bar']` needs to be multi-line
  - `{}`, `{foo: 'bar'}` are okay, `{foo: 'bar', baz: 1}` needs to be multi-line

## Date display

- The user-configured date format and locale live in the app-config slice (`getDateFormat`/`getDateLocale` selectors). `FyDatePipe`
  (`src/common/pipe/fy-date.pipe.ts`) applies them — always display dates through it, never through Angular's raw `date` pipe.
- `mat-datepicker` inputs get the same formatting via `FyDateAdapter` (`src/common/date/fy-date-adapter.ts`, delegates to `FyDatePipe`),
  provided app-wide as the `DateAdapter` in `app.config.ts`. New date pickers therefore need only `MatDatepickerModule` in the component's
  `imports` and inherit the configured format automatically. Never import `MatNativeDateModule` (or use `provideNativeDateAdapter`) at
  component level — a standalone component's NgModule imports contribute providers to the component injector, so it would silently shadow
  `FyDateAdapter` with the native adapter for that subtree. `MatNativeDateModule` belongs in `app.config.ts` only.
- Keep datepicker inputs `readonly` with click-to-open (see `stock-split.component.html`): `FyDateAdapter` only overrides `format()`, so
  typed input would fall through to the native `parse()` and not respect the configured format.

## Templates

- Only the modern control-flow syntax (`@if`/`@else`/`@for`/`@switch`). `*ngIf`/`*ngFor`/`*ngSwitch` must not be used in new or edited
  templates.
- New components are always standalone.
- Tag line-wrapping rule: a tag is "too long" if at least one of the following is true:
  - it has more than one attribute
  - exactly one attribute together with at least one child
  - more than one child.
    A tag that is not too long (meets none of above conditions) stays on one line. Otherwise: the first attribute stays on the opening-tag
    line, every further attribute gets its own line, every child gets its own line, and the closing tag gets its own line. Example (from
    `data-range-selection.component.html`):
  ```html
  <mat-button-toggle (click)="setDataRange('max')"
                     [checked]="dataRange() === 'max'">
    Max
  </mat-button-toggle>
  ```

## Styling (SCSS units)

- Use `rem` for CSS lengths (font-size, padding, margin, gap, width/height, border-radius, border-width, letter-spacing, etc.) in every
  `.scss` file and inline template style. Never write `px` or `em` — use `rem` from the start rather than authoring in `px`/`em` and
  converting later.
- The only accepted exception is a value that must deliberately scale with the font-size of the *same element it's declared on* (e.g. a
  `border-radius`/`padding` pair that keeps a pill/badge shape proportional to that badge's own `font-size`, itself set in `em`) — see
  `performance-label.component.scss` and the `.performance-positive`/`.performance-negative` rules in `position-list.component.scss` for the
  pattern. Add a one-line comment explaining the coupling when you use it, and don't reach for it otherwise.
- Avoid inline styling. Only if it supports readability, this is allowed. E.g. when dividing `width` among columns.

## Testing

The focus on testing in the angular app is on logic. Use `jest` to test:

- NgRx global store
  - effect functions (contents of `src/store/<slice>/effects`)
  - reducer functions (contents of `src/store/<slice>/reducers`)
  - selector functions (contents of `src/store/<slice>/selectors`)
- signal stores
  - effect functions (contents of `<path>/store/effects`)
  - method functions (contents of `<path>/store/methods`)
  - computed functions (contents of `<path>/store/computed`)
  - if applicable, contents of other subdirectories
- angular pipes: `*.pipe.ts` — instantiate the pipe directly with mocked constructor deps, no TestBed; see `security-name.pipe.spec.ts`
  for the pattern (including mocking a `Store` whose `selectSignal` serves several selectors, dispatched by selector identity)
- If important for the logic, components may also have a unit test; however logic should preferably reside in global or signal stores
- Use marble testing (rxjs `TestScheduler`) for observable logic that resolves purely through rxjs operators/schedulers (mostly effects).
  **Not** when the effect's `mergeMap`/`switchMap`/etc. callback is `async` or otherwise returns a `Promise` — a `Promise` resolves via the
  real microtask queue, not rxjs virtual time, so `TestScheduler` can't observe it deterministically. For those, build the input with a real
  `Observable` (`of`/`throwError`) and assert with `await firstValueFrom(...)` instead; see `load-security.effect.spec.ts` for the pattern.
- In every `*.spec.ts` file, import exactly the jest symbols being used (`afterEach`, `beforeAll`, `beforeEach`, `describe`, `expect`,
  `it`, `jest`, ...) explicitly from `@jest/globals` — never rely on ambient globals, and don't import symbols the file doesn't use.
- Testing philosophy
  - Use mocks wherever possible over real instances of dependencies and function arguments.
  - `beforeEach()` sets up the baseline. The **first test function** in the file is the baseline case itself - no extra arrange, just act +
    assert. Every other test changes exactly one precondition in its own arrange step, then does act + assert.
  - Use nested `describe()` if you want multiple tests with the same alteration from the baseline: the shared alteration goes into the
    nested `describe()`'s own `beforeEach()` — never into the `describe()` body, which runs at collection time, before any `beforeEach()`.
    E.g. if tests only make sense if they alter n > 1 preconditions, then the nested `describe()`'s `beforeEach()` may alter up to n-1
    preconditions and each test case alters exactly one in its own arrange step.
  - TypeScript conventions set forth in this file (line length, strong type safety etc.) also apply for tests.
  - use `toBe(...)` whenever referential equality is important - e.g. when a reducer returns the input state
  - Prefer test data factories over verbose inline object initializers. Once a generated/domain type is used by more than one spec, add a
    `<name>Factory(overrides?: Partial<Type>): Type` function for it in `src/testing/` (one file per type, e.g. `security-read.factory.ts`,
    re-exported via `src/testing/index.ts`), returning a fresh object with sensible defaults and spreading `overrides` last so individual
    tests only specify the fields they care about.
