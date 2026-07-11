# LLM.md

This file provides guidance to LLM coding agents (Claude Code, etc.) when working with code in this repository.

## What this package is

`fynancials-api` contains the OpenAPI 3.0.3 specs that are the source of truth for the
Fynancials HTTP API. It has no runtime code of its own — it's purely specs plus validation
scripts. It's consumed as a codegen input by `fynancials-client-angular` (Angular client) and
`fynancials-server-spring` (Spring server delegates); see the root `LLM.md` for that
cross-package workflow.

## Specs

One YAML file per domain, each fully self-contained (no `$ref`s across files — common shapes
like `Id` and `Version` schemas are duplicated per file rather than shared):

- `depot.yaml`, `depot-dividend.yaml`, `depot-performance.yaml`, `depot-position.yaml`, `depot-transaction.yaml`
- `configuration.yaml`, `configuration-security-group.yaml`
- `security.yaml`, `historical-security-price.yaml`
- `notification-dividend-announcement.yaml`

Each declares its own `info.version` (currently `1.0.0` across all files, independent of each other and of the package's own `version` in
`package.json`) and the same local dev `servers` entry (`http://localhost:23726`, matching the Spring backend's port).

## Commands

- `npm run test` — validates every spec (`openapi-generator-cli validate`) in one run.
- `npm run test:depot`, `npm run test:security`, etc. — validate a single spec file; each spec
  has its own `test:<name>` script in `package.json`, this is the fastest way to check one file
  while editing it.
- `openapi-generator-cli` version is pinned per-package in `openapitools.json`
  (`generator-cli.version`) and shared via `generator-cli.storageDir` (`../openapigen`) with
  the other packages — bump it there if consumers need a newer generator, and keep it in sync
  with the plugin versions used in `fynancials-client-angular` and `fynancials-server-spring`.

## Editing conventions

- Changing or adding an endpoint here has no effect on the frontend/backend until they regenerate
  their clients — see the "Making an API change" workflow in the root `LLM.md`.
- Keep new domains consistent with existing ones: a top-level `info`/`servers` block, tags
  matching the domain name, and locally-defined `Id`/`Version` component schemas rather than
  importing them from another file.
- Add new domains to the test script in package.json in accordance with the existing test
  scripts
