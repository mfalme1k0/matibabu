# Continuous integration

Workflow: [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml)

## What runs

| Job | When | Checks |
|-----|------|--------|
| **Backend** | `backend/**` changes | `./mvnw compile` → `test` → `package` (Java 25, Maven wrapper) |
| **Frontend PWA** | `frontend-pwa/**` changes | `npm ci`, typecheck, production build |
| **Frontend (Next)** | `frontend/**` changes | `npm ci`, lint (non-blocking), build |
| **Docs** | `docs/**`, README, `requests.http` | OpenAPI YAML parses if present |
| **CI success** | always | Aggregates results for branch protection |

Path filters avoid running Maven when only the PWA changed (and vice versa).

## Local parity

```bash
# Backend (from repo root)
cd backend
./mvnw -B test
./mvnw -B -DskipTests package

# PWA
cd frontend-pwa
npm ci
npm run typecheck
npm run build
```

Tests use `backend/src/test/resources/application.properties` (in-memory SQLite).
Do not rely on `matibabu-local.db` or IntelliJ run configs for CI green builds.

## Branch protection (recommended)

On `main`:

1. Require status check **CI success** (the aggregator job).
2. Require up-to-date branches before merge (optional but good).
3. Do not require individual matrix jobs by name — they skip when paths do not match.

## Out of scope (for now)

- CD / deploy to servers
- Docker image publish
- Nightly dependency scans (Dependabot remains separate)
- Mutation testing / coverage gates (add when suite is stable)

Deploy automation should wait until this CI stage is reliably green on main.
