# HTTP API

The OpenAPI specs in `traquity-api/` are the source of truth for every HTTP API, and they are intended to be public: third-party
clients and servers may be built against them. That makes them a compatibility surface this project cannot renegotiate
unilaterally after 1.0.0, so the records below fix the conventions that a reader would otherwise have to infer from whichever
endpoint they happened to look at first.

Mechanical conventions (`409 Optimistic Lock failed`, empty collections as `200 []`, path parameters on the path item, `500` never
declared) live in `traquity-api/LLM.md`. What lands here are the decisions that could defensibly have gone the other way.

## ADR-001: Monetary and ratio values are `number` without `format`

**Status:** ACCEPTED

**Decision:** Properties carrying money, quantities of shares, or ratios are declared `type: number` with **no** `format`. `format`
survives only where the value is genuinely integral — `int64` for IDs, versions and day counts, `int32` for years, quarters, months,
page numbers and similar counters.

**Rationale:** The generator maps a bare `number` to `BigDecimal` and `number`/`format: double` to `Double`. Every monetary value in
the server's own domain is already a `BigDecimal` — there is not one `double` field in `traquity-server-spring/src/main/java` — so
`format: double` would insert a lossy narrowing at the MapStruct boundary and nowhere else.

**Consequences, accepted:**

TypeScript is unaffected — `typescript-angular` generates `number` for both spellings — so this is invisible on the client and the
whole benefit lands on the Java side. Jackson serializes `BigDecimal` unquoted, so the wire format does not change either; what
changes is that a value written as `123.10` no longer comes back as `123.09999999999999`. Third-party clients in languages that
parse JSON numbers into doubles get no protection from this, which is a property of JSON rather than of this API.

## ADR-002: Amounts do not carry a currency; the depot selection defines it

**Status:** ACCEPTED

**Decision:** Responses aggregating over a set of depots — `/depot-dividends`, `/depot-performance`, `/depot-performance/income`,
`/depot-positions` and the transaction endpoints — do not repeat a currency next to each amount, and no currency field is added to
`Dividends`, `Performance`, `DepotPerformance`, `DepotValue` or `TransactionRead`. Where a response already carries a currency it
keeps it.

**Rationale:** Every one of these endpoints requires the client to name the depots it is asking about, and rejects a selection whose
depots do not all share a currency with `400`. The currency of the answer is therefore always exactly the currency of the depots the
client itself selected, and the client necessarily holds that already — it had to fetch the depots to have their IDs. Repeating it
per amount would be denormalization of a value the caller supplied.

**Consequences, accepted:**

An amount taken out of its response — logged, cached, forwarded — is no longer self-describing, and a consumer that does this must
carry the currency alongside it. The retained occurrences are deliberate rather than inconsistent, and each is a value that is *not*
derivable from the request: `Lot.currency` and `DepotComposition.currency` are the aggregate root of their response rather than a
per-amount repetition, and `HistoricalSecurityPrice.currency` reports the currency the prices were actually converted *to*, which
may differ from the one requested.

## ADR-003: Single-value state with no merge semantics is last-write-wins

**Status:** ACCEPTED

**Decision:** Two resources mutate without a `version` and declare no `409`, where every other mutation in the API is guarded by
optimistic locking:

- `PUT`/`DELETE` on `/depots/{id}/logo` and `/securities/{id}/logo`
- `PUT` on `/notifications/dividend-announcements/{id}`, whose only writable field is the `isNew` read flag

**Rationale:** For logos, the body is binary `image/png`, so it would have to travel in a header or query parameter, which is a worse
contract than admitting the resource is last-write-wins. Also, the logo's are only meant for the presentation layer. Overwriting does not
harm consistency of the app's data and calculations.

Dividend announcements can only ever receive one update: They can be marked as 'read', but never can go back to the 'unread' state.
Therefore, when two racing writes occur, the outcome will be the same no matter which request comes first and second.

## ADR-004: `DELETE` is idempotent for value resources and `404`s for entities

**Status:** ACCEPTED

**Decision:** Deleting something that is not there is `204` when the resource is a value hanging off a parent — a logo, a client
config key, a client's whole config — and `404` when the resource is an entity with its own identity and lifecycle: depots,
securities, transactions, security groups, data sources, announcement configs.

**Rationale:** The two cases answer different questions. For a value resource the caller is expressing a desired end state ("this
depot has no logo"), and reporting failure for a state that already holds would force every caller to pre-check. For an entity the
caller is naming a specific thing, and a `404` is the only way to tell them that the thing they named does not exist — which for a
delete is precisely the case where silence is dangerous, because the caller may be deleting the wrong ID.

**Consequences, accepted:**

The split has to be looked up rather than derived, which is why it is written down here and in `traquity-api/LLM.md`. A client
writing a generic "delete any resource" helper has to treat `404` as success for entities if it wants uniform behavior, and the API
does not do that for it.

## ADR-005: Naming conventions for paths, parameters and quantity properties

**Status:** ACCEPTED

**Decision:**

- **Paths** are kebab-case throughout, including compound words: `/historical-prices`, not `/historicalprices`.
- **Depot-scoped aggregate collections** are prefixed with the resource they aggregate over: `/depot-positions`,
  `/depot-performance`, `/depot-dividends`.
- **Query parameters carrying identifiers** are named `<resource>Ids`: `depotIds`, `securityIds` — never the bare plural resource
  name.
- **A quantity qualified as absolute or relative** puts the quantity first and the qualifier last: `buyInAbsolute`,
  `currentSizeRelative`, `performanceAbsolute`, `performanceRelative`.
- **Operation IDs** are `get*` / `create*` / `update*` / `delete*`, with `set*` reserved for `PUT` on a value resource that carries
  no version (a logo, a config scalar). A `GET` is never named `is*`.
- **Schema names are singular** unless the schema is itself a collection.

**Rationale**: The main concern is to find a set of rules to enforce consistency across all APIs.

## ADR-006: Spec versions carry no meaning; the release tag is the version

**Status:** ACCEPTED

**Decision:** Everything in `traquity-api/` is versioned together with the app (ADR-013), and nothing inside the specs versions itself.
OpenAPI 3.0.3 requires `info.version`, so the field stays for syntactic reasons only: it reads `1.0.0` in every spec and is never bumped,
whatever changes in that file. The same holds for `version` in `traquity-api/package.json`, a `private: true` package published nowhere.
The version of this API is the release tag it was read from.

**Rationale:** A per-spec version is only worth having if a consumer can hold one spec at one version and another at a different one, and
ADR-013 rules that out — the files travel as one directory in one tag. There must be one single source of truth established.

Deleting the field is not an option. `info.version` is required by OpenAPI 3.0.3, and `openapi-generator-cli validate` — which is what
`npm run test` runs over every spec — rejects a file without it.

**Consequences, accepted:**

A reader who takes `1.0.0` at face value reads it as "API v1", and nothing in the file itself contradicts them — this record and
`traquity-api/LLM.md` are the only places that do. The placeholder is deliberately not kept equal to the app's version either: syncing ten
files on every release would be churn for a number the tag already carries, and one forgotten copy would be worse than an obviously inert
value.

## ADR-007: `401` and `403` are on the contract before the mechanisms behind them exist

**Status:** ACCEPTED

**Decision:** Every operation in every spec declares `401` and `403`, referring to one `Unauthorized` and one `Forbidden` response
defined in that spec's own `components` (ADR-008 forbids reaching into another spec for them).

**Both say the server MAY answer this way, never that it has to.** A deployment that does not authenticate never emits `401`, and one
that does not authorize never emits `403`. What the declaration buys is that no client is entitled to be surprised by one.

No spec declares a `securityScheme` or a `security` block yet, because the mechanisms behind these codes are decided but not built:

- **TLS is decided and planned.** The API is served over plain HTTP today.
- **Registration for third-party apps is decided and planned.** A registered app is either **read-only** or **read+write** — which is
  precisely why both codes matter and neither substitutes for the other: `401` says the caller is unknown, `403` says a known caller has
  been registered for less than it just attempted.

Neither of these arriving is a major release; both are announced here, at 1.0.0. What remains a major release is a **tenant concept**:
there is no user, account or tenant anywhere in the model — no owner on `Depot` or any other aggregate root, no request scoping beyond the
IDs a caller passes itself — and that is not planned.

**Rationale:** The shipped product is a single-user desktop app whose backend binds `127.0.0.1` and lives and dies with its window, so
today the process boundary is the access control: any process that can reach the loopback interface can call the API. But the specs are a
product in their own right (ADR-013), and the cheapest moment to put a failure mode on a public contract is before anyone depends on its
absence. A client that handles `401` and `403` from the first release keeps working when the registration mechanism ships; a client written
against a spec that stayed silent about them has to be rewritten, and would be within its rights to call that a breaking change.

The `401`/`403` split is worth stating because it is the read-only registration that gives `403` a job. Without it a server would only ever
have "who are you?" to answer; with it, a read-only app issuing a `PUT` is a fully authenticated caller doing something it may not, and
collapsing that into `401` would tell it to re-authenticate, which cannot help.

What is deliberately *not* on the contract is `429`. Rate limiting is a property of a deployment rather than of this API: the shipped app
throttles nothing.

Tenancy is the opposite case, and the difference is where it lands. Authentication puts a layer in front of the endpoints; a tenant changes
what the endpoints *mean*. `GET /depots` stops being "the depots" and becomes "this tenant's depots", every aggregate root grows an owner,
and no client written against the current model keeps working merely by sending one more header. That is a redesign of the resource model,
not a convention breached by accident, so ADR-013's patch rule has nothing to say about it and the major update is the honest answer.

**Consequences, accepted:**

Two responses are declared that the shipped app does not return, and a reader of a single endpoint cannot tell from the spec whether the
deployment they are talking to implements them. Generated clients gain no code for them either, since neither carries a body — the codes
surface as ordinary HTTP errors — so the whole benefit is that a client author reads them and writes a handler. A client that skips that
handler is no worse off than today and no better off later.

A client written against 1.0.0 may still have to acquire credentials to keep working once registration ships: the codes were announced, the
obligation to hold a registration was not avoided by announcing them. That cost is accepted in exchange for stating it here rather than in
a release note.

Anyone implementing these specs server-side for more than one user — the SaaS case ADR-013 names — supplies tenancy themselves, and their
surface is then a superset of this one rather than the same API. Nothing here reserves paths, headers or property names for that, so two
such implementations will not agree with each other.

## ADR-008: Every spec is self-contained; no `$ref` crosses a file

**Status:** ACCEPTED

**Decision:** A spec references only its own `components`. There is not one `$ref` to another file anywhere in `traquity-api/`, and there
will not be one: a shape several domains need is defined again in each file that needs it.

**Rationale:** One file per domain is only worth something if the file is the unit — readable, reviewable, generatable and shippable on its
own. A cross-file `$ref` would turn each generator's input from a file into a graph, and both consumers here generate per spec into a
namespace of its own: `src/gen/api/<domain>/` on the client, `de.as.traquity.<domain>.api.model` on the server. A shared `Currency` would
have to be generated into some further place both then import from, which is a package this project has already decided not to publish
(ADR-013). It buys a third party the same property: take the one YAML for the domain you care about, generate, done — no resolver, no
sibling files, no chance that a spec you did not read changes the client you did generate.

This decision is in accordance with the general architecture principle to avoid tight coupling between domains at the cost of DRY. It
provides the opportunity to have clients or servers to include only the specs they want to implement. This could be the case for micro
frontends or microservices.

**Consequences, accepted:**

DRY is violated in favor of avoiding tight coupling between domains.

## ADR-009: A scalar body is `text/plain` when it is a string and JSON otherwise

**Status:** ACCEPTED

**Decision:** Where the entire request or response body is one scalar, the media type follows the scalar's type:

- `text/plain` when it is a string — `GET`/`PUT` on `/config/clients/{clientId}/{clientConfigKey}`, `GET /config/currencies/default`
- `application/json` when it is not — `GET`/`PUT` on `/admin/dev-mode` (a boolean), `GET /admin/pid` (an integer)

**Rationale:** A JSON string body is a quoted, escaped string. A client wanting to store the value `dark` would have to write `"dark"`, and
every reader would have to unquote it, for a payload with no structure to carry — pure ceremony over a media type that transmits the string
as itself. A non-string scalar has no such free representation: `false` or `48936` in a `text/plain` body is a string the client must parse
against rules nobody wrote down, whereas JSON already defines exactly how a boolean and a number are spelled and every generated client
already decodes them.

**Consequences, accepted:**

The media type of a scalar endpoint has to be read off the endpoint rather than assumed from the fact that it is a scalar. And a
`text/plain` body has no framing, so an empty body and an empty string are the same request — deliberate for a client config value, whose
schema allows `minLength: 0`, and the reason such an endpoint cannot later grow a "unset it" meaning for the empty body without colliding
with a value a client may legitimately store.

## ADR-010: Page-based pagination, only where the client cannot bound the collection itself

**Status:** ACCEPTED

**Decision:** Two collection endpoints paginate — `GET /securities` and `GET /depots/{depotId}/transactions` — through optional `page`
(zero-based, default `0`) and `pageSize` query parameters, answering with a `Paginated<X>Read` wrapper carrying `total`, `currentPage`,
`lastPage`, `pageSize` and `items`. Every other collection endpoint answers with a bare array.

An endpoint added later paginates if and only if all three of these hold:

1. its collection grows without bound with the user's history,
2. the request offers no parameter with which a client can bound it itself, and
3. the collection is a stored list read out, rather than a whole computed per request.

**Rationale:** The two paginated collections are the ones that only ever grow: every security a user ever adds, every transaction they ever
record. The unpaginated ones are bounded by something the caller already holds or already narrows — the positions of the depots named in
`depotIds`, the configured data sources, the supported currencies — or by a filter the request carries, the way
`GET /securities/{securityId}/historical-prices` does with `startDate`, which is a better instrument than a page number for a series a
client wants a date range of anyway.

Two collections look like candidates on size alone, and each fails a different clause:

- **`GET /depot-performance`** returns one value per weekday since the first transaction and takes no date range, so it satisfies the
  first two clauses and fails the third: it is not a stored list being read. The endpoint answers by replaying every transaction of the
  selected depots, which is what lets a client ask for *any* combination of depots rather than only the combinations someone thought to
  precompute. Each day's value is derived from the day before it, so no page could be produced without producing every page before it —
  pagination would repeat the entire replay per request, add round trips, and hand the client fragments of a series it needs whole to draw
  one chart. It would cost more than it saves on both sides.
- **`GET /notifications/dividend-announcements`** fails the first clause: it never accumulates, because it is an announcement for a payment
  that has not happened yet, so the collection holds what lies ahead rather than a history and is bounded by that and the securities
  configured for announcements.

Page numbers rather than cursors, because both paginated collections are also sorted on a client-chosen key (`order`, `orderBy`) and are
rendered as tables with a page control. A cursor is the better answer for a feed consumed forward under concurrent writes, which is not
this, and it cannot express "jump to the last page" — which the `lastPage` field exists to support.

**Consequences, accepted:**

Paging over a collection being written to can show an item twice or skip one when a row is inserted between two requests. For a
single-user desktop app whose only writer is the user in front of it, that window is accepted.

More consequential: the choice is effectively frozen per endpoint at 1.0.0. Retrofitting pagination onto an endpoint that returns a bare
array changes its response from an array to a wrapper and breaks every client that reads it, and going the other way is just as bad — so
"does this collection paginate?" is answered when the endpoint is written, not when it turns out to be slow.

## ADR-011: Host-process endpoints are their own spec, and still public

**Status:** ACCEPTED

**Decision:** Endpoints describing the running backend process rather than the user's portfolio live in a spec of their own, `admin.yaml`,
under an `/admin` prefix: `/admin/pid`, `/admin/dev-mode`, `/admin/database` and `/admin/third-party-licenses`. They remain part of the
same public API and are not hidden from the third parties ADR-013 addresses.

Which spec a new endpoint belongs in is decided by what it describes: the process serving the request and the machine under it — its
identity, its mode, its database connection, the dependencies it was built from — is `admin.yaml`; anything describing the portfolio, or
configuration traveling with the database file, is a domain spec.

**Rationale:** The split makes optionality expressible rather than only documentable. A deployment that cannot honestly serve host-process
endpoints now omits one file instead of a subset of one, and the omission is stated by the file it does not ship rather than by a caveat
in one it does. Because ADR-008 already makes each file the unit of generation, the separation propagates on its own: a client generates
`admin.yaml` or does not, and the server's `de.as.traquity.admin` package holds exactly the endpoints a hosted implementation would drop.

Making them public rather than "internal" is unchanged and is still the honest reading of ADR-013: something implementing these specs
elsewhere cannot fake a PID or an H2 console, and pretending the endpoints do not exist would not change that.

**Consequences, accepted:**

A second spec costs a file, a `test:admin` script, a client generator and a Maven execution, and every endpoint added from here on has to
be placed on a boundary ("is this the portfolio or the host?") that is less obvious in the moment than it looks in retrospect. A
misplacement is a breach of this record and, per ADR-013, a patch to correct.

The API remains not implementable in full by an arbitrary deployment, and a client that assumes it is will break against one. Clients
therefore treat the admin endpoints as optional and degrade — a missing PID hides a control, it does not stop a screen. `/admin/database`
already practises this within the shipped app by answering `404` where no web interface exists.

These endpoints also still make the API describe the machine it runs on: `DatabaseConfig` carries a `fileLocation`, a `connectionString`
and a `password` that is non-empty in development mode. Behind the loopback binding of ADR-007 that is a local secret exposed to a local
caller, and it is precisely the surface a hosted implementation must not reproduce — which is now expressible as "does not serve
`admin.yaml`" rather than as a per-endpoint caveat.

## ADR-012: Client configuration is an opaque per-client string map

**Status:** ACCEPTED

**Decision:** `/config/clients/{clientId}` and `/config/clients/{clientId}/{clientConfigKey}` are a key/value store the server persists and
never interprets: keys and values are strings, `clientId` is a free string the client picks for itself (`angular-material` today), and the
only structure the API imposes is the optional `prefix` filter on the collection `GET`. No key is declared anywhere in the specs, no value
is validated beyond being a string, and nothing in the server changes behavior because of what is stored here.

**Rationale:** The alternative is the server knowing about the client's screens — a `theme` field, a `lastOpenedPage` field, a schema per
preference — which is the coupling the root `LLM.md` forbids in the other direction too: presentation belongs to the tier that presents.
An opaque map keeps every UI decision on the client side, where adding a preference costs a key and no API change at all, and keeps the
`clientId` dimension meaningful — a second client (a different UI, a third-party one) stores its own preferences without colliding with,
or having to understand, the first one's.

That the server is the store at all, rather than the client using its own local storage, is what makes preferences travel with the
database: the settings belong to the portfolio file, not to the machine that happens to open it.

**Consequences, accepted:**

There is no way to enumerate what a client may store, no validation, no migration and no garbage collection: a key written by a version
that no longer exists stays until something deletes it, and `DELETE /config/clients/{clientId}` — dropping the client's whole
configuration — is the only broad instrument for that. Values are typed as strings, so a client serializing a number or a JSON object into
one owns both directions of that conversion, and a malformed value can only be discovered by the client that wrote it. The server is also
storing data it cannot reason about, which means it cannot help: no report, no defaulting, no consistency check.

## ADR-013: The API is delivered as repository content at a release tag, not as its own artifact

**Status:** ACCEPTED

**Decision:** The API is a product in its own right — third parties may write clients against it, and anyone may implement it
server-side, e.g. as a SaaS — but it is not delivered anywhere as a build artifact. There is no `@traquity/api` npm package, no Maven
artifact and no separately published spec. Consumers retrieve `traquity-api/` directly from the repository at a release tag.

The API is versioned with the app rather than on its own, and API changes contribute to this project's semver. What forces a **major**
release — of the entire app, since there is no separate API version to bump — is a change that alters one of the API ADRs released up to
that point in a non-backwards-compatible way. Everything else follows the usual rules: additive API changes are a minor release, corrections
a patch. An endpoint that breaches an existing ADR by accident and is fixed afterwards is a **patch**, not a major release.

**Rationale:** Publishing an artifact is a distribution channel, and a channel has to be paid for continuously: a registry account, a
release cadence of its own, a version number readers must reconcile with the app's, and a deprecation policy for the artifact rather
than for the API. The only consumers generating from these specs today are the two parts of this monorepo, and both read the YAML
straight from the working tree at build time. A git tag is already an immutable, addressable coordinate, so the repository is the
distribution channel and it costs nothing extra.

A published artifact would also have to choose what it contains, and neither choice helps. Shipping the raw YAML adds nothing over
the tag. Shipping a generated client fixes one language, one generator and one set of generator options onto consumers who have their
own — the generator choice belongs to whoever consumes the API, exactly as it does for the Angular client and the Spring backend here.

Tying the API's version to the app's is the same argument applied to version numbers: a second, independently moving version would
have to be mapped onto app releases by every reader, and the mapping would have to be published and maintained. Since the app is what
gets released, the release tag answers "which API is this?" without a lookup table.

That the ADRs — not the endpoints as they happen to be — are the compatibility promise is what makes the breach-then-fix case a patch.
An endpoint contradicting an accepted ADR was never inside the promised surface; treating its correction as breaking would make every
slip permanent, or cost a major release for a mistake. This is deliberately narrow: it covers a breach of a recorded convention, not a
deliberate redesign of an endpoint that conformed.

**Consequences, accepted:**

Several release tags may point at the same API, because not every release changes it. The app's version therefore does not tell a
consumer whether the API moved; comparing `traquity-api/` between two tags does. Conversely, an API-driven major bump raises the app's major
version even when nothing user-facing changed, and the release notes have to say so, or users will look for a change that is not there.

An API correction reaches consumers only when the app releases — there is no way to ship the spec on its own schedule. A third party
pinned to a tag whose API breaches an ADR can be broken by a patch release; the risk is accepted, communicated to anyone building against
the API and accepted in favor of polluting the API's / app's major versions for bugfixing.