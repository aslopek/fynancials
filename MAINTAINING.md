# Maintaining

Maintainer-only runbook for operational procedures that aren't obvious from the code.

## Release automation & `RELEASE_TOKEN`

Releases are driven by four GitHub Actions workflows:

- `.github/workflows/dependabot-auto-merge.yml` — enables auto-merge on green Dependabot PRs.
- `.github/workflows/dependabot-rebase-behind.yml` — on every push to `main`, tells Dependabot to rebase the open PRs that fell behind.
- `.github/workflows/dependabot-auto-release.yml` — on a merged Dependabot PR, waits 15 minutes and then dispatches `release.yml` with
  `bump=patch`, but only once no other Dependabot PR is still open. `github-actions` updates are skipped: they change only CI, never what
  ships.
- `.github/workflows/release.yml` — bumps the version, tags, builds, and publishes the GitHub Release.

`dependabot-auto-merge.yml`, `dependabot-rebase-behind.yml` and `release.yml` all use a Personal Access Token stored as the secret
**`RELEASE_TOKEN`** instead of the default `GITHUB_TOKEN`. This is deliberate: GitHub suppresses cascading workflow runs for events caused
by `GITHUB_TOKEN`, so a merge made with the default token would **not** trigger the release chain. Attributing the merge to a PAT makes the
`pull_request: closed` event cascade into `dependabot-auto-release.yml`. `dependabot-rebase-behind.yml` needs the PAT for a second reason:
Dependabot only obeys `@dependabot` commands from an account with write access, and `github-actions[bot]` does not count.

### The two-store gotcha

`RELEASE_TOKEN` must exist in **two separate secret stores**, because Dependabot-triggered workflow runs are sandboxed and can only read
Dependabot secrets — not Actions secrets:

| Store          | Used by                                       | Settings location                                 |
|----------------|-----------------------------------------------|---------------------------------------------------|
| **Actions**    | `release.yml`, `dependabot-rebase-behind.yml` | Settings → Secrets and variables → **Actions**    |
| **Dependabot** | `dependabot-auto-merge.yml`                   | Settings → Secrets and variables → **Dependabot** |

Updating one and forgetting the other breaks the release cascade **silently**: `secrets.RELEASE_TOKEN` resolves to an empty string in the
store where it's missing, and the auto-merge step fails with a cryptic

```
gh: To use GitHub CLI in a GitHub Actions workflow, set the GH_TOKEN environment variable.
##[error]Process completed with exit code 4.
```

If you ever see that, the Dependabot copy of the token is missing or expired.

### Required token permissions

Issue the **fine-grained PAT** with enough scope to merge PRs, comment on them, and dispatch workflows: repository permissions
`Contents: read/write`, `Issues: read/write`, `Pull requests: read/write`, `Workflows: read/write`.

`Issues: read/write` is the counter-intuitive one, since the workflow that needs it only ever touches pull requests. A PR conversation
comment is stored as an *issue* comment — a PR is an issue with a diff attached — and `gh pr comment` posts it through the GraphQL
`addComment` mutation, which is generic over every commentable subject and is therefore gated on `Issues`, never on `Pull requests`.
Without it, `dependabot-rebase-behind.yml` fails with:

```
GraphQL: Resource not accessible by personal access token (addComment)
```

Dropping `Issues: read/write` (e.g. when rotating the token) breaks only `dependabot-rebase-behind.yml`, and only once a Dependabot PR
actually falls behind. Note the expiry.

### Rotating the token

Do this whenever the PAT expires, is revoked, or you're rotating on a schedule. **Both** stores must be updated in the same pass.

1. Create a new PAT with **all** permissions listed above
2. Update the **Actions** secret:
   ```
   gh secret set RELEASE_TOKEN --repo aslopek/fynancials
   ```
3. Update the **Dependabot** secret:
   ```
   gh secret set RELEASE_TOKEN --app dependabot --repo aslopek/fynancials
   ```
4. Verify both are present:
   ```
   gh secret list --repo aslopek/fynancials
   gh secret list --app dependabot --repo aslopek/fynancials
   ```
5. Smoke-test: re-run the failed check on an open Dependabot PR (or comment `@dependabot rebase`) and confirm the
   auto-merge → merge → auto-release → release chain completes. If no Dependabot PR is open, the next push to `main` at least exercises
   `dependabot-rebase-behind.yml` — it must finish green rather than on `addComment`.

## The Dependabot PR queue

Two kinds of Dependabot PRs land here, and they behave differently:

- **Version updates** — on the shared Saturday 06:00 `Europe/Berlin` slot, one PR per `updates` entry via its
  `<part>-version-updates` group (`applies-to: version-updates`), with a 7-day cooldown and no majors. They cover only the dependencies
  declared in `package.json` / `pom.xml`, never transitive ones.
- **Security updates** — triggered by Dependabot alerts, so they ignore both the schedule and the cooldown, and they do reach transitive
  dependencies. The `<part>-security-updates` group (`applies-to: security-updates`) in each `updates` entry bundles them, giving at most
  one PR per ecosystem *and directory*. Grouping across directories additionally requires the repository-level "Grouped security updates"
  setting (Settings → Advanced Security).

`applies-to` is what keeps these apart: a security update is never folded into a version-update PR, so two PRs per directory is the floor
even when both arrive in the same wave.

So a single alert scan can still open more than one PR, and they merge one at a time — the `main` ruleset requires branches to be up to
date. Every merge (and every release commit) therefore pushes the remaining PRs into the `BEHIND` state, which GitHub does **not** resolve
on its own for Dependabot branches: it hands the rebase back to Dependabot, and Dependabot declines to rebase a PR whose bump is already
covered by an existing PR. Without a nudge the queue deadlocks with green checks and auto-merge enabled. That nudge is
`dependabot-rebase-behind.yml`; the manual equivalent is commenting `@dependabot rebase`.

**Consequence of the release debounce:** a merged Dependabot PR releases 15 minutes later at the earliest, and only if no Dependabot PR is
open by then. Merges inside that window cancel each other's pending run, so a wave releases once, from its last merge. A PR that stays open
because its CI is red pauses automatic releases until it is fixed or closed. Dispatch `release.yml` manually if a release is needed sooner.

**GitHub Actions updates never release.** The `github-actions` ecosystem still gets its own weekly PRs and still auto-merges, but
`dependabot-auto-release.yml` ignores them on both counts — merging one does not dispatch a release, and one sitting open does not hold a
release back. Both halves are needed: skipping only the trigger would let a `github-actions` PR that merges last swallow the release the
npm/maven batch had earned. The discriminator is the branch prefix `dependabot/github_actions/`, so the
`dependabot/<manager>/<directory>/<group>-<hash>` branches of the npm and maven entries all keep releasing. A merged `github-actions` PR
still pushes `main`, so the other open PRs are rebased as usual.
