# Maintaining

Maintainer-only runbook for operational procedures that aren't obvious from the code.

## Release automation & `RELEASE_TOKEN`

Releases are driven by three GitHub Actions workflows:

- `.github/workflows/dependabot-auto-merge.yml` — enables auto-merge on green Dependabot PRs.
- `.github/workflows/dependabot-auto-release.yml` — on a merged Dependabot PR, dispatches `release.yml` with `bump=patch`.
- `.github/workflows/release.yml` — bumps the version, tags, builds, and publishes the GitHub Release.

`dependabot-auto-merge.yml` and `release.yml` both use a Personal Access Token stored as the secret **`RELEASE_TOKEN`** instead of the
default `GITHUB_TOKEN`. This is deliberate: GitHub suppresses cascading workflow runs for events caused by `GITHUB_TOKEN`, so a merge made
with the default token would **not** trigger the release chain. Attributing the merge to a PAT makes the `pull_request: closed` event
cascade into `dependabot-auto-release.yml`.

### The two-store gotcha

`RELEASE_TOKEN` must exist in **two separate secret stores**, because Dependabot-triggered workflow runs are sandboxed and can only read
Dependabot secrets — not Actions secrets:

| Store          | Used by                     | Settings location                                 |
|----------------|-----------------------------|---------------------------------------------------|
| **Actions**    | `release.yml`               | Settings → Secrets and variables → **Actions**    |
| **Dependabot** | `dependabot-auto-merge.yml` | Settings → Secrets and variables → **Dependabot** |

Updating one and forgetting the other breaks the release cascade **silently**: `secrets.RELEASE_TOKEN` resolves to an empty string in the
store where it's missing, and the auto-merge step fails with a cryptic

```
gh: To use GitHub CLI in a GitHub Actions workflow, set the GH_TOKEN environment variable.
##[error]Process completed with exit code 4.
```

If you ever see that, the Dependabot copy of the token is missing or expired.

### Required token scopes

Issue the **fine-grained PAT** with enough scope to merge PRs and dispatch workflows:  repository permissions `Contents: read/write`,
`Pull requests: read/write`, `Workflows: read/write`.

### Rotating the token

Do this whenever the PAT expires, is revoked, or you're rotating on a schedule. **Both** stores must be updated in the same pass.

1. Create a new PAT with the scopes above and note the expiry.
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
   auto-merge → merge → auto-release → release chain completes.
