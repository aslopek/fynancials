# Backend HTTP Security

The Spring backend is a single-user process on `127.0.0.1:23726`, spawned by the Electron shell for the lifetime of one app run. It has no
login, no session and no cookie of its own, and the desktop app is its only client. Most of what a public web backend has to defend against
therefore has no counterpart here, while one thing a public backend never faces does apply: the renderer that calls it is a `file://` page,
whose `Origin` is the literal string `null`.

## ADR-001: CSRF stays disabled; the origin allowlist is what protects state-changing requests

**Status:** ACCEPTED

**Context:** `SpringSecurityConfig.filterChain` disables Spring Security's CSRF filter, which CodeQL reports as
`java/spring-disabled-csrf-protection` (High). The standard remedy — a synchronizer token in a cookie, echoed back in a header — cannot be
applied to this app:

- The packaged renderer loads over `file://`, so Chromium gives it an opaque origin. Every request it makes to `http://localhost:23726` is
  cross-site, and a cross-site cookie needs `SameSite=None; Secure`, which a plain-HTTP backend cannot set. `CookieCsrfTokenRepository`
  would hand out a token the renderer never receives.
- Angular's built-in XSRF interceptor only attaches `X-XSRF-TOKEN` to relative URLs. Every generated API client targets the absolute
  `http://localhost:23726` from its spec's `servers:` block, so the interceptor would not fire even if the cookie arrived.

Enabling the filter as it stands would `403` every write the shipped app makes.

**Decision:** `csrf` remains disabled. The CORS configuration in the same class is what rejects a foreign origin: `CorsFilter` answers any
request carrying an `Origin` outside `cors.allowed-origins` with `403` before it reaches a controller, preflight and actual request alike.
The CodeQL alert is dismissed as a false positive rather than worked around.

**Rationale:** CSRF is the abuse of ambient authority — a browser attaching a credential the attacker cannot read to a request the attacker
caused. This backend issues no such credential: it has no session cookie, no `Authorization` scheme and no `httpBasic`. A request forged by
another page reaches an endpoint with exactly the authority that page already has by calling it directly, so a token would add nothing that
the origin check does not already provide. Two further properties bound the exposure:

- Every state-changing endpoint is preflighted. `POST` bodies are `application/json`, and `PUT`/`PATCH`/`DELETE` are not simple methods, so
  none of them is reachable by the form-post-without-preflight route. No endpoint accepts `multipart/form-data` or
  `application/x-www-form-urlencoded`. `PUT /config/clients/{clientId}/{clientConfigKey}` takes `text/plain`, which is CORS-safelisted as a
  content type, but `PUT` is preflighted regardless.
- `server.address: 127.0.0.1` keeps the port off the network, so the attacker has to be a page in a browser on the same machine.

**Consequences, accepted:**

- The protection rests on `Origin` being present and honest on cross-origin requests, which is the browser's job rather than this app's. A
  non-browser client on the same machine can call every endpoint unhindered — it always could, and ADR-007 of [API](api.md) already names
  that as the position the shipped app takes: the process boundary and the operating system's user account are the access control until
  the registration mechanism described there ships.
- Adding an endpoint that accepts `multipart/form-data`, `application/x-www-form-urlencoded`, or `text/plain` on a `POST` breaks the second
  bullet of the rationale: such a request is a simple request, and a foreign page can send it without a preflight. The `Origin` check still
  rejects it, but the reasoning above no longer has two independent legs. Revisit this ADR rather than adding the endpoint quietly.
- The CodeQL alert recurs on every scan of a branch touching that line and has to be dismissed again. Removing the alert for good means
  giving the app a token channel that does not depend on cookies — the Electron shell handing a per-run secret to the backend the way it
  already hands over the database password, and to the renderer over the IPC bridge — which is a feature in its own right, not a
  configuration change.
