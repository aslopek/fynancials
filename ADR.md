# Architectural Decision Records

This file contains Architectural Decision Records (ADRs) for the project. Target audience are humans. LLMs shall ignore this file unless
explicitly asked to read from or write to it.

## Dependabot Auto Releases

`dependabot-auto-merge.yml`, `dependabot-rebase-behind.yml` and `dependabot-auto-release.yml` together turn a merged Dependabot PR into a
dispatch of `release.yml`.

### ADR-1: Every dependency update is released, as a patch version, without asking

**Status:** ACCEPTED

**Decision:** A merged Dependabot PR touching npm or Maven dependencies dispatches `release.yml` with `bump=patch` — weekly version
updates and GHSA-triggered security updates alike. No human step sits between the merge and the published release.

**Rationale:** The dependencies ship inside the Electron app, so a bump that is merged but not released has changed nothing for any user.
That matters most for security updates, where the advisory is only answered once the fix is downloadable. Patch is always the right bump,
because the `ignore` rules drop every `version-update:semver-major` before it becomes a PR.

**Consequences, accepted:**

Versions advance on dependency churn alone, and no human sees such a release before it is published — it rests entirely on the CI gate and
the three-OS build in `release.yml`.

### ADR-2: A batch of PRs produces one release, dispatched by the last one to merge

**Status:** ACCEPTED

**Decision:** `dependabot-auto-release.yml` runs on every closed Dependabot PR and dispatches `release.yml` only when no Dependabot PR is
left open against `main`. Every merge in a batch except the last one therefore releases nothing.

**Rationale:** Dependabot arrives in bursts. Releasing per PR would run the full three-OS build each time, producing patch versions that are
known to be patched immediately by the next release.

**Consequences, accepted:**

The gate is a count taken at one moment, which leaves two accepted holes:

- A release fires early if the first PR of a batch merges before the last one is opened — narrow, since full CI would have to beat
  Dependabot's own PR creation, but a race nonetheless.
- A batch whose final PR is *closed* rather than merged dispatches nothing, leaving the earlier merges unreleased until some later merge
  drains the queue.

Both can be mitigated by hand, since `release.yml` is a `workflow_dispatch`.

### ADR-3: GitHub Actions updates never trigger a release

**Status:** ACCEPTED

**Decision:** PRs on `dependabot/github_actions/*` branches are excluded from `dependabot-auto-release.yml`, both as a trigger and from the
count it waits for.

**Rationale:** Nothing in `.github/workflows` ships, so such a release would be identical to its predecessor. The exclusion must cover the
count too: an open github-actions PR would otherwise hold the batch back indefinitely, since merging it releases nothing.

### ADR-4: Each ecosystem gets its own Dependabot groups, all sharing one weekly slot

**Status:** ACCEPTED

**Decision:** Every entry in `.github/dependabot.yml` carries its own `schedule` and its own `applies-to: version-updates` and
`applies-to: security-updates` groups, all on one slot: Saturday 06:00 `Europe/Berlin`. No `multi-ecosystem-group`.

**Rationale:** The multi-ecosystem group gave a tidier weekly PR, but it also claimed every security update these entries generated and
mishandled it twice over. Its branch names omit the directory, so on 2026-08-04 PRs #49 and #52 — the same advisory in two directories —
collided on one name. And every job touching such a PR is dispatched as a refresh of the group, finds no group PR to refresh, and no-ops:
that made `@dependabot rebase` useless and left #50, #51 and #53 unable to catch up with `main`, hence unable to merge at all under the
up-to-date requirement. Per-ecosystem groups restore the standard `dependabot/<manager>/<directory>/<group>-<hash>` namespace, in which
both work.

One shared slot is what preserves ADR-2's one release per wave. Saturday morning leaves the weekend to react.
