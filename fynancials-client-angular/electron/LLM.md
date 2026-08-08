# LLM.md

This file provides guidance to LLM coding agents (Claude Code, etc.) when working with code in this repository.

## What this directory is

The Electron main process: the desktop shell that owns `fynancials.config.json`, resolves Java, spawns the Spring backend jar as a child
process, and opens the `BrowserWindow` on the built Angular app (`../dist/fynancials/browser/index.html`). `main.js` is the entry point
named by `package.json`'s `"main"`.

Paths here are relative to `electron/`, so `__dirname` needs a `'..'` to reach the package root — that is where `dist/`, `resources/` and
`node_modules/` live.

See the parent `LLM.md` for the Angular renderer, and the root `LLM.md` for how the three monorepo parts fit together.

## Module layout

```
electron/
  main.js                      entry point; wiring only, no logic worth testing
  preload.js                   contextBridge surface shared with the renderer
  config/
    config-schema.js           zod schemas + inferred types for fynancials.config.json
    config-file.js             read/write fynancials.config.json
    auth.js                    pure scrypt records: create / classify / verify
    auth-registry.js           the auth map over a loaded config, keyed by database base path
  backend/
    backend-reachable.js       poll GET /config/pid until reachable or child exit
    backend-process.js         spawn, log piping, single-instance guard, proven-start recording; (#40) stdin password handover
  java/                        (#38) resolve-java.js, download-corretto.js
  ipc/
    ipc-schema.js              zod schema for IPC input crossing the renderer boundary
    startup-bridge.js          registers `startup:getState` and `backend:start`
  window/
    startup-mode.js            computes the startup mode (`boot` | `configure` | `unlock`) and consumes `configureOnNextStart`
    main-window.js             `BrowserWindow` creation, wired to `preload.js`
  testing/                     shared spec-only helpers (custom matchers, ...) used by more than one *.spec.js
```

Modules marked with an issue number do not exist yet; the story bringing them is named in brackets. Keep this map current as they land — it
is what a reader starts from.

## Boot order

`app.on('ready')` loads the config, computes the startup mode (`window/startup-mode.js`), registers the IPC bridge (`ipc/startup-bridge.js`)
and opens the single `BrowserWindow` (`window/main-window.js`). The backend is spawned only when the renderer calls `backend:start` over
that bridge, handled by `backend/backend-process.js`, which resolves with an outcome (`reachable` or not) the renderer routes on. Java
resolution (`verifyJava()` in `main.js`) still runs lazily, right before that spawn, and is otherwise untouched until #38 replaces it.

The preload (`preload.js`) runs in Electron's sandboxed preload context, where `require` is a limited polyfill resolving only `electron`
and a handful of Node built-ins — it cannot `require` a module of this app. That is why the two IPC channel names (`startup:getState`,
`backend:start`) are literals duplicated in both `preload.js` and `ipc/startup-bridge.js` rather than shared via an import; keeping them in
step is part of the manual verification checklist below.

`config/config-file.js`'s `load()` no longer creates and writes the config file when none exists — it returns the default configuration
without saving. Distinguishing "file missing" from "file present" is `exists()`, added to `ConfigFile` for that purpose: the startup-mode
computation is what decides a missing file means `configure` mode, and that mode must leave no write behind.

## Structure rule

There is **no integration-test harness for Electron main code**. Everything with logic therefore lives in a module of its own that takes
its I/O dependencies as arguments (file system, `fetch`, timers, the spawned child), and `main.js` is wiring: it constructs the
collaborators, passes the real implementations, and connects them to Electron's `app` events. A piece of logic that can only be reached by
booting Electron is a piece of logic nothing will ever test — that is why the directory looks the way it does.

## Type safety — JSDoc over checked JavaScript

Electron loads `main.js` directly, so these files are CommonJS rather than TypeScript. That is a constraint on the *syntax*, not on the
type checking: **every file under `electron/` is fully type-checked by `tsc` in strict mode, with all types expressed as JSDoc**.
`tsconfig.electron.json` is the single switch (no `// @ts-check` pragmas in the files), and it covers `*.spec.js` as well. Untyped
JavaScript is not an accepted state anywhere in this directory: no `any` in any form, no `@ts-ignore`, no `@ts-nocheck`. `@ts-expect-error`
is allowed only in a spec that deliberately asserts a type error, with a comment naming which one.

Why the overrides in `tsconfig.electron.json` are what they are — none of this should be "cleaned up" later:

- **`module`/`moduleResolution: node16`** — the base config's `ES2022` + `bundler` is rejected together with CommonJS and would mistype
  `require`. `package.json` has no `"type"` field, so `node16` correctly treats these files as CJS.
- **`lib: ["ES2023"]`, no `DOM`** — the main process has no DOM, so `document`, `window` and `localStorage` fail to compile here instead of
  failing at runtime. `fetch`, `Response` and friends come from `@types/node`.
- **`noUncheckedIndexedAccess`** — makes an index access carry `| undefined`, which is what turns "a database with no `auth` entry is
  pending" into something the compiler enforces rather than something a doc asserts.
- **`exactOptionalPropertyTypes`** — an optional property may be absent, but not present-and-`undefined`. Build option bags by omission
  (`{...(logger ? {logger} : {})}`), not by assigning `undefined`; where a property genuinely means "explicitly not set", declare it as
  `@property {X | undefined} [key]` rather than reaching for the flag.
- **`strict`, `noImplicitReturns`, `noFallthroughCasesInSwitch`, `noPropertyAccessFromIndexSignature`** are inherited from the base config.
  Keep them.

JSDoc rules:

1. **Every exported function has a complete signature** — one `@param {Type} name` per parameter, one `@returns {Type}`. `@param {Object}`
   and `@returns {*}` are not types.
2. **Every internal data shape is a named `@typedef`**, declared once in the module that owns it: option bags, injected-dependency shapes,
   return objects. Import them across files with `@import {X} from './y.js'` next to the `require` calls — never re-declare.
3. **Model states as unions and literal types**, not as loose objects and `string` (`@typedef {'pending' | 'passwordless' | 'scrypt'}
   AuthState`), so classifying becomes a narrowing operation.
4. **Injected dependencies get a minimal declared type** — the same principle the parent `LLM.md` states for selectors. A module needing
   three `fs` functions declares exactly those three (see `ConfigFileSystem`), rather than depending on the whole module type. The real
   dependency is structurally assignable to it, and — the actual payoff — so is a test stub made of exactly that many `jest.fn()`s, with no
   cast anywhere. A stub missing one is a compile error. This holds just as much for **this app's own collaborators** as for a third-party
   module: an injected `ConfigFile`, `AuthRegistry` or `BackendProcess` is declared as a `Pick<>` of the members the module actually calls
   (`Pick<ConfigFile, 'save'>`), never as the whole exported typedef. Naming the whole type costs nothing at runtime and everything in a
   spec — the stub then has to carry members the module cannot reach, and a reader of that spec is left wondering which of them the
   behavior depends on.
5. **Type assertions** are written `/** @type {X} */ (expression)`, parentheses required, and only where no parser can help (a dynamic
   `require`). Never assert to `any`.
6. **Constant objects use `@satisfies`**, which checks the shape without widening the literal types away.
7. **Electron and Node types come from the real packages**: `/** @type {import('electron').BrowserWindow | null} */`,
   `/** @type {import('node:child_process').ChildProcessWithoutNullStreams | null} */`.
8. Document *meaning* in prose only where the type cannot carry it (encodings, units, "base path without extension"). A comment restating
   the type is noise.

## The zod boundary rule

**Anything arriving from outside the program — disk, IPC, the network, a subprocess — is defined exactly once, as a zod schema, with its
static type inferred from that same schema via `z.infer`.** A hand-written `@typedef` for such a shape is an *assertion about* the data
while the validating code is a *separate* piece of logic; nothing keeps the two in agreement, and they drift the moment a key is added.
`JSON.parse` returns `any` and is the one real hole in checked JS: its result goes straight into a `safeParse` and never flows on untyped.
Do not hand-roll a type predicate for it either.

`config/config-schema.js` owns every schema and every exported type for `fynancials.config.json`. Later keys (`configureOnNextStart`,
`java`) extend that one file rather than adding parsers of their own, and every consumer's static type follows automatically.

**Everything your own code constructs stays a `@typedef`.** The test for a new type: *does a value of this shape ever arrive from disk,
IPC, the network or a subprocess?* Then zod. Otherwise `@typedef`. Reasons this is a line rather than a preference, so it does not get
re-litigated:

- Shapes constructed three lines earlier (`ConfigFileOptions`, `BackendStartOutcome`, `AuthState`) have no untrusted input to validate. A
  schema for them runs at every call for a check that cannot fail, and buys a type a one-line `@typedef` already states.
- The injected-dependency types **cannot** be zod: `ConfigFileSystem` is a bag of functions, `z.function()` in zod 4 is a function factory
  rather than a schema and cannot be a field in `z.object()`, and validating a function at runtime would be meaningless anyway. That
  typedef exists so a drifted *test stub* fails `tsc` — which is the right time to fail.
- A schema must read as a **signal**. Where zod appears, a reader concludes "untrusted input arrives here". Spread over internal shapes, it
  stops carrying that meaning while the real boundaries blend in.

Two rules the config schemas specifically depend on: an entry map is validated **per entry**, so one mangled entry makes that entry
unusable rather than throwing away the whole file; and the semantic checks that protect a *call* (resource bounds around `scryptSync`) stay
in the calling module, not in the schema — zod answers "is this shaped like a record", not "is this safe to run".

## Runtime dependencies

`package.json`'s `dependencies` block is the main process's. Anything the main process `require`s statically belongs there and **not** in
`devDependencies`: electron-packager prunes dev dependencies out of the packaged app, which is exactly why `custom-electron-prompt` needs
the manual `postPackage` copy in `forge.config.js` and a dynamic `require` (that pattern is unresolvable for the type checker — do not copy
it for anything new).

A package added to `dependencies` ships to users, and `scripts/generate-third-party-licenses.js` picks it up from that block on its own —
attribution is not something to remember here. Its license still has to pass `npm run licenses:check` and the root `LLM.md`'s licensing
rule. The reverse direction is the one that needs care: a package required only by a spec stays a `devDependency` (`expect`, used by
`testing/base64-of.js`, is the example) and must never be moved into `dependencies` to make something resolve — that would ship a test
library to users and put it in the license report.

## What of this directory ships

`forge.config.js`'s `packagerConfig.ignore` is an **allowlist**: `package.json`, `electron/`, `dist/fynancials/browser/` and
`node_modules/` go into `app.asar`, and everything else in the package directory — the Angular sources, the build tooling, a second copy of
`backend.jar` (it is already delivered as an `extraResource`) — does not. A file added anywhere else therefore ships only once someone says
so, instead of shipping because nobody looked.

Within `electron/`, three things are carved back out: `*.spec.js`, everything under `testing/`, and the `*.md` files. Test code in a
shipped app cannot even load — its dependencies are pruned away with the other devDependencies — and its only remaining effect would be to
suggest a test library is part of the product. Keep new spec-only helpers under `testing/` so they stay covered by that rule.

Note that a function-valued `ignore` *replaces* electron-packager's built-in default patterns rather than extending them, which is why
`node_modules/.bin` is excluded explicitly. Anything else worth keeping out of the package belongs in the same list.

## Testing

Specs are `electron/**/*.spec.js`, plain CommonJS, run by `jest.electron.config.ts` via `npm run test:electron` — which type-checks the
directory first, so a type error fails fast instead of surfacing as a confusing runtime failure. The suite needs nothing but `npm ci`: no
Angular build, no generated API clients, no backend jar, no Java.

The shared conventions from the parent `LLM.md`'s Testing section apply unchanged — explicit `@jest/globals` imports, `beforeEach`
establishes the baseline, the first test in the file *is* the baseline case, every other test changes exactly one precondition in its own
arrange step, shared alterations live in a nested `describe`'s own `beforeEach`, mocks over real dependencies. What is specific here:

- `jest.electron.config.ts` sets `injectGlobals: false`, because jest's injected `jest` wrapper argument collides with the explicit
  `const {jest} = require('@jest/globals')` in a CommonJS file. Import every jest symbol; there are no ambient globals to fall back on.
- The parent `LLM.md`'s custom-asymmetric-matcher convention applies here too, with `electron/testing/` standing in for `src/testing/` as
  the shared location (there is no `src/` in this directory) — see `testing/base64-of.js` for the worked example: a class extending
  `expect`'s `AsymmetricMatcher`, JSDoc-typed like everything else here, with its own `base64-of.spec.js`.
- Specs are type-checked like everything else. Declare the mock functions standalone, assemble them into the declared dependency type, and
  assert on the standalone bindings:

  ```js
  const existsSync = jest.fn(() => true);
  const readFileSync = jest.fn(() => storedContents);
  const writeFileSync = jest.fn();

  /** @type {ConfigFileSystem} */
  const fileSystem = {existsSync, readFileSync, writeFileSync};
  ```

  Reaching for `/** @type {any} */` on a stub means the stub and the real dependency have diverged — fix the stub.
- A stub that stands in for something asynchronous must model *waiting*, not just resolving. A `delay` stub resolving immediately turns a
  poll loop into a microtask spin that starves the event loop; return a promise that stays pending instead.
- **The specs and the shipped app do not run on the same crypto.** `npm run test:electron` runs under plain Node, which statically links
  its own OpenSSL; the packaged app runs under Electron, which builds Node against Chromium's BoringSSL instead (`process.versions.openssl`
  reads `0.0.0` there — that is the tell). Both are bundled into the respective binary, identically on Windows, macOS and Linux, so this is
  a *runtime* difference and never a platform one: no system `libssl` is ever involved.

  Anything here touching `node:crypto` therefore carries a gap no spec can close. Code that relies on a rule enforced *inside* one of those
  libraries must **degrade rather than throw** when the other one disagrees — `config/auth.js` is the worked example: its resource bounds
  mirror scrypt's own parameter validation so a hand-edited record classifies as pending, and the derivation is additionally guarded so an
  unanticipated rejection reads as "does not verify" instead of escaping into the startup path. To settle an actual disagreement, probe the
  shipped runtime directly: `ELECTRON_RUN_AS_NODE=1 npx electron <script>` runs any script under Electron's own Node and crypto.

## Manual verification

Main-process changes have **no integration coverage** — the end-to-end behavior has to be run by hand, and in the *packaged* app
(`npm run electron:pack`), not only under `npm run electron:start`. A pruned-away runtime dependency, a missed `__dirname` fix or a
resource path that only exists in the repo checkout all pass every test and fail on the first real start.
