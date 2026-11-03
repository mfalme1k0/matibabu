# Matibabu

**Offline-first Electronic Medical Records for low-connectivity clinics**

[![CI](https://github.com/mfalme1k0/matibabu/actions/workflows/ci.yml/badge.svg)](https://github.com/mfalme1k0/matibabu/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Matibabu is built for healthcare facilities where continuous internet access cannot be assumed. Each facility runs its own node against a local database, so clinical work carries on regardless of the network. Synchronization between facilities is treated as a separate concern, designed in [ADR-08](docs/decisions/ADR-08-synchronization.md).

---

## Contents

- [Features](#features)
- [Current status](#current-status)
- [Repository layout](#repository-layout)
- [Quick start](#quick-start)
- [Configuration](#configuration)
- [Authentication and roles](#authentication-and-roles)
- [API overview](#api-overview)
- [Testing and CI](#testing-and-ci)
- [Documentation](#documentation)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- **Patients**: registration, update, delete, search, lookup by phone number
- **Encounters**: start, discharge and cancel, with department and attending clinician
- **Medical records**: vitals, clinical observations, diagnoses and treatments, attached to an encounter
- **Referrals**: create, complete and cancel, linked to an encounter and a receiving facility
- **Facilities and departments**: create, list, deactivate
- **Medicines**: searchable catalogue seeded from the Kenya Essential Medicines List (KEML), mapped to WHO ATC codes, with an admin review workflow for unresolved mappings
- **Security**: session-based login, CSRF protection, role-based access control
- **Local persistence**: SQLite for facility nodes, with Flyway migrations
- **API docs**: Swagger UI plus a curated OpenAPI specification

## Current status

| Area | State |
|---|---|
| Backend: patients, encounters, medical records, referrals, facilities, departments, medicines | Implemented |
| Authentication and roles | Implemented (session cookie + CSRF) |
| Web client (`frontend-pwa`) | In progress: login, patient list, registration and patient detail |
| Offline synchronization | Groundwork only: `sync_outbox` table and node/facility identity. No sync client or hub ingestion yet ([ADR-08](docs/decisions/ADR-08-synchronization.md) is *Proposed*) |
| `did:web` authentication | Designed in [ADR-02](docs/decisions/ADR-02-did-web-authentication.md), not implemented |
| Reporting and interoperability (KHIS/DHIS2) | Planned |
| `frontend` (Next.js) | Starter scaffold, not the primary client |

---

## Repository layout

```text
matibabu/
├── backend/          Spring Boot API (Java 25, Maven)
│   ├── src/main/java/com/matibabu/backend/
│   │   ├── api/             REST controllers, request/response DTOs, error handling
│   │   ├── application/     Use cases and services
│   │   ├── domain/          Domain model and repository interfaces
│   │   ├── infrastructure/  JPA entities, mappers, repository adapters
│   │   ├── security/        Login, roles, clinician accounts
│   │   └── config/          CORS, OpenAPI, node identity
│   ├── src/main/resources/db/migration/   Flyway migrations (V1 to V25)
│   └── tools/               KEML to ATC seed-generation scripts
├── frontend-pwa/     React + Vite + Tailwind PWA (primary web client)
├── frontend/         Next.js scaffold
├── docs/             Architecture, ADRs, API, CI and security docs
├── docker-compose.yml
└── requests.http     Example HTTP requests
```

The backend follows Clean Architecture with feature-oriented packaging ([ADR-001](docs/decisions/ADR-001-architecturedecisions.md)). The domain and application layers do not depend on Spring or JPA.

---

## Quick start

### Prerequisites

- Java 25 (the Maven Wrapper is included, so Maven is optional)
- Node.js 22 and npm, if you run the web client
- Docker, if you use Docker Compose

### Run the backend

```bash
git clone https://github.com/mfalme1k0/matibabu.git
cd matibabu/backend
./mvnw spring-boot:run
```

The `local` profile is active by default. It uses SQLite and creates `backend/matibabu-local.db` on first start, and Flyway applies all migrations automatically. The API listens on `http://localhost:8080`.

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Curated OpenAPI spec | http://localhost:8080/openapi.yaml |
| Generated OpenAPI spec | http://localhost:8080/v3/api-docs |

### Run the web client

```bash
cd frontend-pwa
npm install
npm run dev        # http://localhost:3000
```

The dev server proxies `/api`, `/csrf`, `/login`, `/logout` and `/auth` to the backend, so the browser sees a single origin and session and CSRF cookies work without extra CORS setup. To target a different backend:

```bash
MATIBABU_BACKEND=http://192.168.1.10:8080 npm run dev
```

### Run with Docker Compose

```bash
docker compose up --build
```

This builds the backend image and starts a facility node on port `8080`, with data kept in the `matibabu-data` volume. See [Configuration](#configuration) for the environment variables involved.

### First login

Migration `V10` seeds an administrator account with the email `admin@example.com`. **Change this password, or replace the account, before any real deployment.** Further clinicians can be registered through `POST /auth/register` and promoted by an admin.

---

## Configuration

Settings live in `backend/src/main/resources/`. Profiles: `local` (default, SQLite), `prod`, and `test`.

| Property / env var | Purpose | Default |
|---|---|---|
| `DB_URL` | JDBC URL | `jdbc:sqlite:./matibabu-local.db` |
| `app.facility.id` / `APP_FACILITY_ID` | Stable UUID identifying this facility node. Required | Placeholder UUID in `local` |
| `app.node.role` | `facility` (hospital site) or `hub` (national system, see ADR-08) | `facility` |
| `app.cors.allowed-origins` / `APP_CORS_ALLOWED_ORIGINS` | Comma-separated browser origins (no `*`, credentials are enabled) | `http://localhost:3000` |
| `SERVER_PORT` | HTTP port | `8080` |
| `server.servlet.session.timeout` | Idle session timeout | `30m` |
| `server.servlet.session.cookie.secure` | Set to `true` when served over HTTPS | `false` |

The `prod` profile additionally reads `DB_USER`, `DB_PASSWORD` and `FACILITY_ID`.

---

## Authentication and roles

- Form login at `POST /login` with `email` and `password` parameters. A successful login returns `200` and sets a `JSESSIONID` cookie; a failure returns `401`.
- CSRF uses a cookie token. Fetch it from `GET /csrf`, then send it back in the `X-XSRF-TOKEN` header on state-changing requests. Swagger UI does this through a small helper script.
- Roles: `USER`, `ADMIN`, `SUPER_ADMIN`.
    - `/api/admin/**` requires `ADMIN` or `SUPER_ADMIN`.
    - Every other `/api/**` endpoint requires an authenticated user.
    - Public: `/`, `/auth/register`, `/csrf`, `/login`, Swagger UI and the OpenAPI documents.
- A single concurrent session is allowed per clinician.

---

## API overview

The curated OpenAPI spec is the reference ([`docs/api/openapi.yaml`](docs/api/openapi.yaml)); `requests.http` has runnable examples.

| Area | Endpoints |
|---|---|
| Patients | `POST/GET /api/patients`, `GET/PUT/DELETE /api/patients/{id}`, `GET /api/patients/search`, `GET /api/patients/phone/{phoneNumber}` |
| Encounters | `POST /api/encounters`, `GET /api/encounters/{id}`, `POST /api/encounters/{id}/discharge`, `POST /api/encounters/{id}/cancel` |
| Medical record | `POST/GET /api/encounters/{encounterId}/medical-record`, plus `/vitals`, `/observations`, `/diagnoses`, `/treatments` |
| Referrals | `POST/GET /api/encounters/{encounterId}/referrals`, `GET /api/patients/{patientId}/referrals`, `GET /api/referrals/{id}`, `POST /api/referrals/{id}/complete`, `POST /api/referrals/{id}/cancel` |
| Facilities | `POST/GET /api/facilities`, `GET /api/facilities/{id}`, `POST /api/facilities/{id}/deactivate` |
| Departments | `POST/GET /api/departments`, `GET /api/departments/{id}`, `POST /api/departments/{id}/deactivate` |
| Medicines | `GET /api/medicines`, `GET /api/medicines/{id}` |
| Admin | `POST /api/admin/clinicians/{id}/promote`, `GET /api/admin/medicines/needs-review`, `PATCH /api/admin/medicines/{id}/atc-mapping` |
| Auth | `POST /auth/register`, `POST /login`, `POST /logout`, `GET /csrf` |

---

## Testing and CI

```bash
cd backend
./mvnw clean test        # JUnit 5 + Mockito, in-memory SQLite

cd ../frontend-pwa
npm run typecheck
npm run build
```

GitHub Actions ([`ci.yml`](.github/workflows/ci.yml)) runs only the jobs relevant to the files changed:

- **Backend**: compile, test, package (Java 25), upload the JAR
- **Docker (backend)**: multi-stage image build and a smoke check (no push)
- **Frontend PWA**: `npm ci`, typecheck, build
- **Frontend (Next)**: lint (non-blocking) and build
- **Docs**: the curated OpenAPI file must exist and parse
- **CI success**: single gate for branch protection

More detail in [`docs/ci/README.md`](docs/ci/README.md).

---

## Documentation

| Topic | Location |
|---|---|
| Architecture overview | [`docs/architecture/architectureOverview.md`](docs/architecture/architectureOverview.md) |
| Backend architecture | [`docs/architecture/backendarchitecture.md`](docs/architecture/backendarchitecture.md) |
| Testing approach | [`docs/architecture/Testing.md`](docs/architecture/Testing.md) |
| Architecture decision records | [`docs/decisions/`](docs/decisions/) (summary in `ADR-summary.md`) |
| API guides | [`docs/api/`](docs/api/) |
| Security workflow | [`docs/security/Security-Workflow.md`](docs/security/Security-Workflow.md) |
| Facility seeding | [`docs/database/seeding_facilities_database.md`](docs/database/seeding_facilities_database.md) |
| Medicine (KEML to ATC) tooling | [`backend/tools/README.md`](backend/tools/README.md) |
| PWA client | [`frontend-pwa/README.md`](frontend-pwa/README.md) |

---

## Roadmap

1. Web client: encounters, medical records, referrals, facility and department admin
2. Offline sync: outbox writers in application services, sync client, hub ingestion and patient identity matching ([ADR-08](docs/decisions/ADR-08-synchronization.md))
3. Offline write queue in the PWA once the sync design settles. The service worker currently caches the app shell only, and patient data is deliberately not cached
4. Facility and device authentication (`did:web`, [ADR-02](docs/decisions/ADR-02-did-web-authentication.md))
5. Reporting and interoperability with KHIS/DHIS2

---

## Contributing

Contributions, discussions and suggestions are welcome. If you find a bug or have an improvement in mind, open an issue or submit a pull request. Please keep new code consistent with the layering described in [ADR-001](docs/decisions/ADR-001-architecturedecisions.md) and include tests.

## License

Released under the [MIT License](LICENSE).

---

**Matibabu**: building resilient healthcare software for environments where connectivity cannot be guaranteed.