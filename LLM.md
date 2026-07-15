# LLM.md

This file provides guidance to LLM coding agents (Claude Code, etc.) when working with code in this repository.

## Repository overview

Fynancials is a portfolio-tracking desktop app. This is a monorepo; three parts matter:

- **fynancials-api** — OpenAPI 3 specs, the source of truth for every HTTP API. One YAML file per domain.
- **fynancials-client-angular** — Angular + NgRx frontend, packaged as the Electron desktop app that ships to users. Generates its API
  client (`typescript-angular` generator) from `fynancials-api` into `src/gen/api/`.
- **fynancials-server-spring** — Spring Boot backend implementing the same APIs. Generates server-side delegate interfaces (`spring`
  generator) from `fynancials-api` at build time into `target/generated-sources/openapi`.
- **openapigen/** ignore it - it's just the shared cache dir for the `openapi-generator-cli` jar (`storageDir` in both `openapitools.json`
  files).

See `fynancials-api/LLM.md`, `fynancials-client-angular/LLM.md`, and `fynancials-server-spring/LLM.md` for details specific to each part.

## Code style

Line length is capped at 140 characters per line, project-wide (all three parts). Language-specific code styles are defined in the
respective `LLM.md` files.

## Dependency licensing

This project is MIT-licensed. Only add a new dependency (npm or Maven, in any of the three parts) if its license is compatible with that —
permissive licenses (MIT, BSD, Apache-2.0, ISC, 0BSD, BlueOak-1.0.0, etc.) are fine. Do not add a dependency under a copyleft license
(GPL/AGPL/LGPL) or a source-available/non-OSI license (e.g. BSL, FSL, Commons Clause) without explicitly flagging it and getting
confirmation first — check the dependency's own `package.json` `license` field (npm) or its POM's `<licenses>` block (Maven, inherited from
its parent POM if not set directly)before adding it.

## How the pieces fit together at runtime

The Electron app (`fynancials-client-angular`) is the shipped product. Its `main.js` spawns a bundled Java process running the Spring Boot
backend (`backend.jar`) as a child process, then points the Angular UI at it. The backend listens on port `23726` (H2 console on `23727`),
backed by a local encrypted H2 file database whose path is configurable via `FY_DB_FILE_PATH`.

`forge.config.js` copies `fynancials-server-spring/target/fynancials-server-spring-<version>.jar` into
`fynancials-client-angular/resources/backend.jar` during electron-forge packaging — the Spring backend must be built (`mvn package`) before
an Electron package/make build.

## Making an API change (cross-cutting workflow)

1. Edit/add the relevant OpenAPI YAML in `fynancials-api/`.
2. Regenerate the Angular client: `cd fynancials-client-angular && npm run generate`.
3. Rebuild the Spring backend: `cd fynancials-server-spring && mvn generate-sources` (or `compile`/`package`) — the
   `openapi-generator-maven-plugin` runs in the `generate-sources` phase and produces one delegate interface per domain (e.g.
   `DepotApiDelegate`), implemented by a package-private `*Controller`.
4. Keep the `@openapitools/openapi-generator-cli` version in sync across `fynancials-api/package.json`'s devDependency and
   `fynancials-client-angular/package.json`'s devDependency.
5. Keep `fynancials-api/openapitools.json`, `fynancials-client-angular/openapitools.json` and the plugin version in
   `fynancials-server-spring/pom.xml` in sync when bumping.

Upon adding a new API spec file in step one, the following steps must be conducted before continuing to generate code:

1. Add a test script to `fynancials-api/package.json` and reference the test script in `scripts.test`
2. Add a generator to `fynancials-client-angular/openapitools.json`
3. Add an execution to plugin `org.openapitools.openapi-generator-maven-plugin` in `fynancials-server-spring/pom.xml`

## Commands and per-part architecture

Commands (build, run, test) and architecture details for each part live in that part's own
`LLM.md` — read the one for the part you're working in before making changes there.

## Git

Use `gh` and `git` commands to interact with the GitHub repository.

Use of any read-only `git` or `gh` commands (e.g. `git status`, `git diff`, reviewing PRs) is permitted.

You may never perform writing operations such as, but not limited to: creating/amending commits, pushing, creating/deleting branches,
creating/deleting PRs etc. Keep the most important rule in mind: Anything going into this repository - code, text, PRs, issues etc. - are my
responsibility and fully owned by me, which is why you cannot write any content yourself.

### PR reviews

When I ask you to do a PR review, first check source and target branch:

- Merging from `next-release` to `main` is done in preparation of a release.
- When a PR merges from any other branch to `main` you have to flag this to me, since the HEAD of `main` is supposed to always represent the
  latest release (what is bundled therein - documentation may update the HEAD without a release).
- You may assume that code on `next-release` has already been reviewed through other PRs, but a quick sanity check should include:
    - Is everything mentioned on `CHANGELOG.md`'s `[Unreleased]` Section actually contained in the PR?
    - Is anything missing in the `[Unreleased]` section, that's actually on the list of commits to be merged?
  - Bug/Feature/NFR issues are worth mentioning in the `CHANGELOG.md`. issues tagged only as `technical improvement` don't get
      mentioned.

## Drafting and Reporting

Larger output such as plans, PR reviews etc. go to the `.scratchpad` directory as Markdown files.

When I ask you to draft stories, you first create a directory in `.scratchpad`. Within this directory you put for every story a separate
file. Use the following files as templates:

- bugs: `.github/ISSUE_TEMPLATE/bug_report.md`
- features: `.github/ISSUE_TEMPLATE/feature_request.md`
- NFRs: `.github/ISSUE_TEMPLATE/non_functional_requirement_request.md`

If it makes sense to provide multiple stories, create a meta file in the directory. This may be turned into an epic and contain things like
architectural decision records (ADRs), story map and other useful information.
