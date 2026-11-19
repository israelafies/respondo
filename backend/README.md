# Respondo Worker backend

This directory contains the Cloudflare Worker API foundation and the initial Cloudflare D1 schema. It is independent of the Android `app/` module.

## Architecture and current scope

```text
Android app (Room remains local) -- future HTTPS requests --> Cloudflare Worker --> D1
```

The Android app does not connect directly to D1. This milestone provides only health, safe system information, and a D1 connectivity check. It does not add Android networking, account authentication, synchronization, user-data APIs, AI generation, provider adapters, or message sending. No existing Room records are copied or changed.

The Worker uses its native `fetch` entry point, typed Cloudflare bindings, a small JSON response layer, an origin allowlist, and Wrangler-managed D1 migrations. No web framework or database ORM is needed for the current API surface. The future AI service boundary and provider-secret rules are documented in [`src/services/ai/README.md`](src/services/ai/README.md); no provider is implemented or operational.

## Directory structure

```text
backend/
  migrations/0001_initial_schema.sql  # initial D1 schema
  src/
    db/                                # D1 access boundary
    lib/                               # JSON response helpers
    middleware/                        # CORS handling
    routes/                            # health and system routes
    services/ai/README.md              # future server-side provider boundary
    types/                             # Worker environment bindings
  tests/                               # Worker runtime endpoint tests
  .dev.vars.example                    # non-secret local variables template
  package.json                         # scripts and pinned toolchain ranges
  package-lock.json                    # npm dependency lockfile
  tsconfig.json
  vitest.config.ts
  wrangler.toml                        # Worker and D1 bindings/environments
```

## Requirements and installation

- Node.js `>=22.12.0` and npm. The CI workflow uses Node.js 22.
- Cloudflare authentication is **not** needed for local tests, local D1, or local Worker development.
- A Cloudflare account and Wrangler authentication are needed only to create remote D1 databases, configure Worker secrets, apply remote migrations, or deploy.

From this directory:

```sh
npm ci
cp .dev.vars.example .dev.vars
```

`.dev.vars` and `.env` files are ignored by Git. The checked-in example has placeholders/settings only and contains no credentials. Do not paste provider keys into Android source, Wrangler vars, the repository, or GitHub workflow logs.

## Local development

```sh
npm run db:migrate:local
npm run db:verify:local
npm run dev
```

Wrangler runs the Worker locally and stores local D1 state under the ignored `.wrangler/` directory. The local API is normally available at `http://localhost:8787`.

Example requests:

```sh
curl http://localhost:8787/health
curl http://localhost:8787/api/v1/system/info
curl http://localhost:8787/api/v1/system/database
```

The local `CORS_ALLOWED_ORIGINS` default allows `http://localhost:3000`. Change it to the exact browser development origins in `.dev.vars`; native Android requests do not rely on browser CORS. An unset or empty allowlist grants no cross-origin browser access. CORS is not authentication.

## D1 schema and migrations

The binding name is `DB`; the local/default database name is `respondo-db`. The initial migration creates:

| Table | Purpose and relationships |
| --- | --- |
| `users` | Cloud account identity placeholder; unique email, no password column or login implementation. |
| `user_settings` | Non-secret provider selection mode, optional selected provider, and response tone; owned by a user. |
| `prospects` | Cloud-side prospect fields corresponding to current business context; owned by a user. |
| `conversations` | Conversation metadata, optionally linked to a prospect under the same user. |
| `messages` | Conversation message content and role; composite foreign key keeps it within the owning user. |
| `ai_provider_settings` | Provider enablement, priority, and model preference; it contains no API credentials. |
| `activity_logs` | User-owned action metadata for a future audit trail; callers must avoid storing secrets or unnecessary message content. |

Foreign keys are enforced by D1. Child rows use cascade deletion where data is wholly owned by a user or conversation; a prospect referenced by conversation history is restricted from deletion until that reference is handled. Indexes support owner-scoped and chronological reads. Timestamps are stored as UTC ISO-8601 text.

This is a cloud foundation, not a Room synchronization migration. It does not yet model the Room Offer Profile and Campaign entities or contain any existing local records. Those mappings and conflict rules must be designed before sync endpoints are introduced.

Create and apply the local database:

```sh
npm run db:migrate:local
npx wrangler d1 migrations list respondo-db --local
```

Create separate remote databases when a Cloudflare account is configured:

```sh
npx wrangler d1 create respondo-db-staging
npx wrangler d1 create respondo-db
```

Copy each ID returned by Cloudflare into the corresponding `database_id` in `wrangler.toml` (default/production and staging). The committed values are explicit placeholders, not real IDs. Do not attempt a remote migration or deploy until real IDs have replaced them and the account is authenticated.

After configuring real IDs, remote migrations are:

```sh
npm run db:migrate:remote
npx wrangler d1 migrations apply respondo-db-staging --remote --env staging
```

Review the selected environment and database carefully before any remote operation. Remote D1 and Worker deployment have not been run as part of this milestone.

## Worker environments and deployment

`wrangler.toml` defines local/default development, `staging`, and `production` variables and bindings. Production CORS starts empty because no production browser origin is known. Add only exact trusted origins if a browser client is introduced; native Android does not require CORS. Staging and production use separate database-name placeholders.

Once Cloudflare account access and database IDs are available:

```sh
npx wrangler login
npm run db:migrate:remote
npx wrangler d1 migrations apply respondo-db-staging --remote --env staging
npx wrangler deploy --env staging
npx wrangler deploy --env production
npx wrangler tail
```

Use Cloudflare Worker secrets for future provider/API credentials, for example `npx wrangler secret put GROQ_API_KEY --env production`. That secret is not consumed in this milestone. No deployment workflow or automatic remote deployment is configured.

To inspect bindings and validate/build the Worker locally:

```sh
npm run config:check
npm run bundle:check
```

## API endpoints

All responses use `{ "success": boolean, "data": object|null, "error": object|null }` and JSON content type.

| Method and path | Behavior |
| --- | --- |
| `GET /health` | Returns service status, service name, timestamp, and API version. Does not query D1. |
| `GET /api/v1/system/info` | Returns application/API version, environment name, and non-sensitive capability names. |
| `GET /api/v1/system/database` | Runs `SELECT 1` through `DB`; returns connected status or a generic 503 without SQL details. |

Unknown routes return a generic 404. Unsupported methods on a known route return 405. Oversized declared request bodies are rejected. There are no user-specific routes yet.

## Security boundaries

- D1 is only accessed from Worker code through the `DB` binding.
- CORS uses exact configured origins; no wildcard is used. CORS does not authenticate native or browser clients.
- Responses include `Cache-Control: no-store`, `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, and `Referrer-Policy: no-referrer`.
- Database failures return a generic response. Logs contain only fixed failure labels, not SQL errors, request bodies, prompts, or credentials.
- Authentication, authorization, account isolation at the API boundary, rate limiting, abuse controls, and production security review are not implemented. Do not expose prospect, conversation, message, or settings routes until those controls exist.
- No provider secret is required or configured yet. Future keys belong in Worker secrets, never D1, tracked files, Android, or ordinary Wrangler variables.

## Tests and checks

```sh
npm run typecheck
npm test
npm run db:migrate:local
npm run db:verify:local
npm run config:check
npm run bundle:check
```

The Worker Vitest plugin applies the checked-in migration to its local D1 binding before tests. Tests verify all seven application tables, expected indexes, a clean foreign-key check, invalid/orphaned and cross-user relationships, restrictive prospect deletion, child/user cascades, endpoint envelopes, CORS, request validation, safe D1 failure mapping, and the absence of unauthenticated user-data routes. CI also applies the migration with Wrangler to its local D1 store; `db:verify:local` checks tables, indexes, constraints, and deletion behavior against that store; then Wrangler runs the Worker and checks `/api/v1/system/database` against the migrated database. These checks require a supported Workers runtime and do not access a Cloudflare account.

## Troubleshooting

- **Wrangler reports a D1 ID issue:** Replace the explicit placeholders with IDs returned by `wrangler d1 create`; never fabricate an ID.
- **CORS preflight returns 403:** Add the exact browser `Origin` to `CORS_ALLOWED_ORIGINS`; do not use `*` as a workaround.
- **Local Worker database is empty:** Run `npm run db:migrate:local` and confirm the local `.wrangler/` state is writable.
- **Remote commands require login:** Run `npx wrangler login` in an authorized environment and confirm the selected account/database before proceeding.
- **Tests cannot start `workerd`:** Check that the host can run Cloudflare's local Workers runtime and that Node meets the minimum version.
