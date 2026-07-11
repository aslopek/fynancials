# LLM.md

This file provides guidance to LLM coding agents (Claude Code, etc.) when working with code in this repository.

## Repository overview

Fynancials is a portfolio-tracking desktop app. This is a monorepo; three parts matter:

- **fynancials-api** — OpenAPI 3 specs, the source of truth for every HTTP API. One YAML file per
  domain.
- **fynancials-client-angular** — Angular + NgRx frontend, packaged as the Electron desktop app
  that ships to users. Generates its API client (`typescript-angular` generator) from
  `fynancials-api` into `src/gen/api/`.
- **fynancials-server-spring** — Spring Boot 4 backend implementing the same APIs. Generates
  server-side delegate interfaces (`spring` generator) from `fynancials-api` at build time into
  `target/generated-sources/openapi`.
- **fynancials-client-node** exists but is unmaintained — ignore it.
- **openapigen/** is just the shared cache dir for the `openapi-generator-cli` jar (`storageDir` in both `openapitools.json` files).

See `fynancials-api/LLM.md`, `fynancials-client-angular/LLM.md`, and `fynancials-server-spring/LLM.md` for details specific to each part.

## Code style

Line length is capped at 140 characters per line, project-wide (all three parts). See
`fynancials-client-angular/LLM.md` for frontend-specific TypeScript/template conventions.

## Dependency licensing

This project is MIT-licensed. Only add a new dependency (npm or Maven, in any of the three parts)
if its license is compatible with that — permissive licenses (MIT, BSD, Apache-2.0, ISC, 0BSD,
BlueOak-1.0.0, etc.) are fine. Do not add a dependency under a copyleft license (GPL/AGPL/LGPL)
or a source-available/non-OSI license (e.g. BSL, FSL, Commons Clause) without explicitly flagging
it and getting confirmation first — check the dependency's own `package.json` `license` field
(npm) or its POM's `<licenses>` block (Maven, inherited from its parent POM if not set directly)
before adding it.

## How the pieces fit together at runtime

The Electron app (`fynancials-client-angular`) is the shipped product. Its `main.js` spawns a
bundled Java process running the Spring Boot backend (`backend.jar`) as a child process, then
points the Angular UI at it. The backend listens on port `23726` (H2 console on `23727`), backed
by a local encrypted H2 file database whose path is configurable via `FY_DB_FILE_PATH`.

`forge.config.js` copies `fynancials-server-spring/target/fynancials-server-spring-1.0.0.jar`
into `fynancials-client-angular/resources/backend.jar` during electron-forge packaging — the
Spring backend must be built (`mvn package`) before an Electron package/make build.

## Making an API change (cross-cutting workflow)

1. Edit/add the relevant OpenAPI YAML in `fynancials-api/`.
2. Regenerate the Angular client: `cd fynancials-client-angular && npm run generate`.
3. Rebuild the Spring backend: `cd fynancials-server-spring && mvn generate-sources` (or
   `compile`/`package`) — the `openapi-generator-maven-plugin` runs in the `generate-sources`
   phase and produces one delegate interface per domain (e.g. `DepotApiDelegate`), implemented
   by a package-private `*Controller`.
4. Keep the `openapi-generator` version in sync across `fynancials-api/package.json`'s
   devDependency, `fynancials-client-angular/openapitools.json`, and the plugin version in
   `fynancials-server-spring/pom.xml` when bumping.

## Commands and per-part architecture

Commands (build, run, test) and architecture details for each part live in that part's own
`LLM.md` — read the one for the part you're working in before making changes there.
