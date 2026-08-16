# LLM.md

This file provides guidance to LLM coding agents (Claude Code, etc.) when working with code in this repository.

## What this package is

`traquity-api` contains the OpenAPI 3.0.3 specs that are the source of truth for the TraQuity HTTP API. It has no runtime code of its
own — it's purely specs plus validation scripts. It's consumed as a codegen input by `traquity-client-angular` (Angular client) and
`traquity-server-spring` (Spring server delegates); see the root `LLM.md` for that cross-package workflow.

## Specs

One YAML file per domain, each fully self-contained: no `$ref`s across files, so common shapes like `Id`, `Version`, `Currency` and
`SortOrder` are defined again in every spec that needs them rather than shared (`architecture/api.md` ADR-008).

Each declares an `info.version` of `1.0.0` — a placeholder OpenAPI requires and this project never bumps, since the API is versioned with
the app and the release tag is its version (`architecture/api.md` ADR-006 and ADR-013); leave it alone when a contract changes. Each also
declares a `title`/`description` naming the one domain the file owns, and the same local dev `servers` entry (`http://localhost:23726`,
matching the Spring backend's port). No two specs share a title.

## Commands

- `npm run test` — validates every spec (`openapi-generator-cli validate`) in one run.
- `npm run test:depot`, `npm run test:security`, etc. — validate a single spec file; each spec has its own `test:<name>` script in
  `package.json`, this is the fastest way to check one file while editing it.
- `openapi-generator-cli` version is pinned per-package in `openapitools.json` (`generator-cli.version`) and shared via
  `generator-cli.storageDir` (`../openapigen`) with the other packages — bump it there if consumers need a newer generator, and keep it in
  sync with `traquity-client-angular/openapitools.json` and the plugin version in `traquity-server-spring/pom.xml`.

## Editing conventions

- Changing or adding an endpoint here has no effect on the frontend/backend until they regenerate their clients — see the "Making an API
  change" workflow in the root `LLM.md`.
- Keep new domains consistent with existing ones: a top-level `info`/`servers` block, tags matching the domain name, and locally-defined
  `Id`/`Version` component schemas rather than importing them from another file.
- **An endpoint describing the running backend process belongs in `admin.yaml`, under `/admin`** (`architecture/api.md` ADR-011) — its
  identity, its mode, its database connection, the dependencies it was built from. Everything describing the portfolio, or configuration
  travelling with the database file, belongs in a domain spec.
- **`PUT` on a version-carrying resource declares a `409` with description `Optimistic Lock failed`; `DELETE` does not.** The `409` means
  "the version you sent is stale" — `PUT` takes a version in its body, so a client can be stale, can be told so, and can re-read and
  retry. No `DELETE` accepts a version anywhere in this API, so there is no client-supplied precondition to fail and nothing a client
  could do differently; declaring it there described a response no request could provoke.
- **`500` is never declared.** Any endpoint can fail unexpectedly. Putting it in the API contract says "expect the unexpected", which
  clients should do. But doing so creates unnecessary noise in the specs.
- **An endpoint returning a collection answers "there is nothing" with `200` and an empty array, never with `204` or `404`.** Such an array
  is therefore declared `minItems: 0`, and a paginated wrapper returns its `items` empty (with `lastPage: 0`) rather than a bodyless
  response. `404` on a collection endpoint is reserved for a missing *parent* resource — `/securities/{id}/stock-splits` returns `404` when
  the security does not exist and `200 []` when it exists without splits. This keeps every consumer on one code path instead of
  special-casing a response that carries no body.
- **A collection paginates only where the caller cannot bound it itself** (`architecture/api.md` ADR-010) — through the optional `page`
  (zero-based, default `0`) and `pageSize` query parameters and a `Paginated<X>Read` wrapper, as `/securities` and
  `/depots/{depotId}/transactions` do. Everything else returns a bare array; adding or removing pagination later breaks the response type,
  so decide it when the endpoint is written.
- **A body that is one scalar is `text/plain` when the scalar is a string and `application/json` when it is not** (`architecture/api.md`
  ADR-009) — a config value and the default currency travel as text, dev mode (boolean) and the PID (integer) as JSON.
- **Every operation declares `401` and `403`** (`architecture/api.md` ADR-007), each as a `$ref` to that spec's own `components/responses`
  (`Unauthorized`, `Forbidden`) — a new operation is not finished without both, and a new spec file copies the two responses rather than
  reaching into another file for them (ADR-008). They say a server *may* answer this way, never that it has to. `429` is deliberately not
  declared: rate limiting belongs to a deployment, not a desktop app. No spec declares a `securityScheme` or a `security` block yet either —
  TLS and the read-only/read+write registration behind these codes are planned but not built, so do not anticipate them in a new endpoint.
- **Naming** (`architecture/api.md` ADR-005): kebab-case paths including compound words (`/historical-prices`); depot-scoped aggregates
  prefixed with what they aggregate over (`/depot-positions`, `/depot-dividends`); query parameters carrying identifiers named
  `<resource>Ids` (`depotIds`, `securityIds`); a quantity qualified as absolute or relative written quantity-first (`performanceAbsolute`,
  `currentSizeRelative`); operation IDs `get*`/`create*`/`update*`/`delete*`, with `set*` reserved for a `PUT` on a value resource that
  carries no version, and never `is*` for a `GET`; schema names singular.
- **A resource that can be created and updated gets the `Create`/`Update`/`Read` triple** — `Update` is `Create` plus `version`, `Read`
  is `Update` plus the identity. `Create` never carries a `version`: there is nothing to lock against yet.
- **Money, share counts and ratios are `type: number` with no `format`** — `format` is only for genuinely integral values (`int64` for
  IDs, versions and day counts; `int32` for years, quarters, months and page numbers). `format: double` makes the Java generator emit
  `Double` and narrows the server's `BigDecimal` at the mapper boundary; see `architecture/api.md` ADR-001.
- **`DELETE` answers `204` for an absent value resource and `404` for an absent entity** (`architecture/api.md` ADR-004). A missing
  *parent* is always `404`, whichever kind of resource hangs off it.
- Optionality is expressed by leaving a property out of `required`, never by `nullable: true`.
- Add new domains to the test script in package.json in accordance with the existing test scripts.
