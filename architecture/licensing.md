# Dependency Licensing

This project is MIT-licensed, and the root `LLM.md` states the rule each new dependency is measured against: permissive licenses are fine,
copyleft and source-available ones need an explicit decision first. Three mechanisms carry that rule, and none of them covers everything:

- `npm run licenses:check` (`traquity-client-angular/scripts/check-third-party-licenses.js`) validates the npm dependencies that ship in
  the Electron app. It runs in CI and again in `release.yml`, and it sees nothing on the Maven side.
- `dependency-review` in `ci.yml` covers npm and Maven alike, but only the dependencies a pull request **adds**. Anything already present
  on the base branch is never re-examined, however its terms may have changed upstream.
- `ThirdPartyLicenseServiceImpl` is attribution rather than enforcement: it enumerates `BOOT-INF/lib/` inside `backend.jar` at runtime and
  serves each library's license, falling back to the canonical texts in `traquity-server-spring/src/main/resources/licenses/` where a jar
  embeds none.

The gap in the second bullet is not hypothetical. Both records below describe dependencies that entered in this repository's initial
commit and were therefore never part of any pull request's diff.

## ADR-1: Liquibase stays on 5.x under FSL-1.1-ALv2

**Status:** ACCEPTED

**Decision:** `liquibase-core` stays pinned at 5.0.3 in `traquity-server-spring/pom.xml` under the Functional Source License
(FSL-1.1-ALv2, ALv2 Future License), and is named in `ci.yml`'s `allow-dependencies-licenses` so the license gate stops failing on it.

**Rationale:** Liquibase was Apache-2.0 through 4.33.0 and relicensed to FSL-1.1-ALv2 from 5.0.1 on. What the FSL restricts is competing
use, and TraQuity is a local-first portfolio tracker rather than a database change-management product, so the use this project makes of it
is not the one the license sets out to prevent. Each FSL release additionally converts to Apache-2.0 two years after its publication. The
library is consumed unmodified, as one jar among many inside `backend.jar`, and its full license text is bundled and served.

**Consequences, accepted:**

This repository's own code stays MIT, but `backend.jar` is not uniformly permissively licensed: anyone redistributing it inherits the FSL
terms for that one component, so "TraQuity is MIT" describes the source in this repository rather than every byte the app ships.

## ADR-2: H2 is exempted as a detector artifact, not as a licensing decision

**Status:** ACCEPTED

**Decision:** `com.h2database:h2` is named in `ci.yml`'s `allow-dependencies-licenses` alongside Liquibase.

**Rationale:** H2's POM declares two licenses, `MPL 2.0` and `EPL 1.0`, and both are already on the `allow-licenses` list.
`dependency-review` reads the pair as a single conjunction and fails to parse the second name, reporting
`LicenseRef-scancode-unknown AND MPL-2.0` — an expression no allow-list entry can match. Nothing about H2's actual terms is at issue.

**Consequences, accepted:**

One list now holds two entries that mean opposite things, which is why removing either of them means reading this file first. Should the
action ever parse H2's declaration correctly, this entry belongs deleted rather than kept for symmetry with ADR-1.
