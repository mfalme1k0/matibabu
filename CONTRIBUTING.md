# Contributing to Matibabu

Thank you for your interest in contributing to Matibabu.

Matibabu is an offline-first Electronic Medical Records (EMR) system designed for healthcare facilities where continuous internet connectivity cannot be assumed. Contributions should help make the system reliable, maintainable, secure, and useful in low-connectivity healthcare environments.

This document explains how to set up the project, work within its architecture, test changes, and submit contributions.

---

## Table of Contents

- [Before You Start](#before-you-start)
- [Development Environment](#development-environment)
- [Getting Started](#getting-started)
- [Repository Structure](#repository-structure)
- [Architecture and Design Principles](#architecture-and-design-principles)
- [Working on Changes](#working-on-changes)
- [Testing](#testing)
- [Database Changes and Migrations](#database-changes-and-migrations)
- [API Development](#api-development)
- [Frontend Development](#frontend-development)
- [Offline-First Development](#offline-first-development)
- [Security and Patient Data](#security-and-patient-data)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Guidelines](#pull-request-guidelines)
- [Reporting Bugs and Requesting Features](#reporting-bugs-and-requesting-features)
- [Project Documentation](#project-documentation)

---

## Before You Start

Before contributing:

1. Read the project [README](README.md).
2. Check existing issues and pull requests to avoid duplicating work.
3. Review relevant Architecture Decision Records (ADRs) before changing architectural behaviour.
4. Open an issue or start a discussion for significant changes before implementation.

For substantial changes, explain:

- The problem being addressed.
- The proposed solution.
- The affected parts of the system.
- Any architectural or database implications.
- How the change will be tested.

Small bug fixes, documentation improvements, and isolated test improvements can generally be submitted directly.

## Development Environment

### Required tools

| Tool | Requirement | Purpose |
|---|---|---|
| Java | 25 | Backend development |
| Maven | Included through Maven Wrapper | Backend builds and tests |
| Node.js | 22 | Frontend development |
| npm | Included with Node.js | Frontend dependencies |
| Git | Recent version | Version control |
| Docker | Optional | Containerized development |

The backend includes a Maven Wrapper, so installing Maven globally is not necessary.

## Getting Started

### 1. Fork and clone the repository

Fork the repository on GitHub, then clone your fork:

```bash
git clone https://github.com/YOUR_USERNAME/matibabu.git
cd matibabu
```

Add the original repository as the upstream remote:

```bash
git remote add upstream https://github.com/mfalme1k0/matibabu.git
```

### 2. Create a branch

Start from the latest upstream changes:

```bash
git fetch upstream
git checkout main
git merge upstream/main
```

Create a branch for your contribution:

```bash
git checkout -b feature/your-feature-name
```

Use descriptive branch names, for example:

```text
feature/patient-search
fix/encounter-validation
test/referral-service
docs/update-api-guide
```

### 3. Run the backend

Navigate to the backend:

```bash
cd backend
```

Start the application:

```bash
./mvnw spring-boot:run
```

The `local` profile is active by default.

The local configuration uses:

- SQLite for persistence.
- Flyway for database migrations.
- `backend/matibabu-local.db` as the local database file.
- Port `8080` for the API.

Flyway applies database migrations automatically when the application starts.

Available API documentation:

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Curated OpenAPI specification | http://localhost:8080/openapi.yaml |
| Generated OpenAPI specification | http://localhost:8080/v3/api-docs |

### 4. Run the PWA frontend

Open another terminal from the repository root:

```bash
cd frontend-pwa
npm install
npm run dev
```

The development server runs at:

```text
http://localhost:3000
```

The PWA development server proxies API requests to the backend.

To use a different backend address:

```bash
MATIBABU_BACKEND=http://192.168.1.10:8080 npm run dev
```

### 5. Run with Docker Compose (optional)

From the repository root:

```bash
docker compose up --build
```

This starts the backend facility node on port `8080`, with persistent data stored in the `matibabu-data` Docker volume.

---

## Repository Structure

The repository is organized into the backend, frontend clients, documentation, and supporting configuration.

```text
matibabu/
├── backend/
│   ├── src/main/java/com/matibabu/backend/
│   │   ├── api/
│   │   ├── application/
│   │   ├── domain/
│   │   ├── infrastructure/
│   │   ├── security/
│   │   └── config/
│   │
│   ├── src/main/resources/
│   │   └── db/migration/
│   │
│   └── tools/
│
├── frontend-pwa/
├── frontend/
├── docs/
│   ├── architecture/
│   ├── decisions/
│   ├── api/
│   ├── ci/
│   ├── database/
│   └── security/
│
├── docker-compose.yml
├── requests.http
├── README.md
├── CONTRIBUTING.md
└── LICENSE
```

### Backend package responsibilities

| Package | Responsibility |
|---|---|
| `api` | REST controllers, request and response DTOs, and error handling |
| `application` | Use cases and application services |
| `domain` | Domain models, business rules, and repository interfaces |
| `infrastructure` | JPA entities, mappers, and repository adapters |
| `security` | Authentication, authorization, roles, and clinician accounts |
| `config` | Application configuration, CORS, OpenAPI, and node identity |

Keep new code in the appropriate package rather than introducing unnecessary top-level packages.

---

## Architecture and Design Principles

The backend follows Clean Architecture with feature-oriented packaging, as documented in [ADR-001](docs/decisions/ADR-001-architecturedecisions.md).

The most important architectural boundary is dependency direction.

- The domain layer must not depend on Spring or JPA.
- The application layer should coordinate use cases without depending on infrastructure implementations.
- Infrastructure implements interfaces defined by the inner layers.
- API controllers should handle HTTP concerns and delegate application behaviour to the appropriate services.

### General development principles

**1. Keep business rules in the appropriate layer.**

Do not place domain rules inside controllers or persistence entities.

**2. Prefer explicit boundaries.**

Use appropriate interfaces, DTOs, and mappings rather than exposing infrastructure details throughout the application.

**3. Avoid unnecessary dependencies.**

Introduce new libraries only when they provide meaningful value and are justified by the project.

**4. Keep changes focused.**

A contribution should solve a clearly defined problem without unnecessarily restructuring unrelated parts of the codebase.

**5. Preserve existing behaviour.**

When changing an existing feature, consider its API contract, persistence implications, security requirements, and dependent features.

**6. Document architectural decisions.**

Significant changes to architecture, data ownership, synchronization, authentication, or integration should be discussed and documented through an ADR.

---

## Working on Changes

A typical contribution should follow this sequence:

1. Understand the existing implementation.
2. Identify the relevant domain and application responsibilities.
3. Review related tests and documentation.
4. Implement the smallest coherent change.
5. Add or update tests.
6. Run the relevant checks.
7. Update documentation where necessary.
8. Submit a pull request.

Before introducing a new abstraction, check whether an existing interface, service, or implementation already handles the responsibility.

Avoid implementing functionality that is only described in the roadmap as though it were already part of the system. Confirm the current implementation and relevant ADR first.

---

## Testing

Testing is an important part of every contribution, particularly because Matibabu handles clinical workflows and persistent patient records.

### Backend tests

Backend tests use JUnit 5 and Mockito, with in-memory SQLite for database-related tests.

From the `backend/` directory:

```bash
./mvnw clean test
```

For a complete Maven verification:

```bash
./mvnw clean verify
```

When contributing backend functionality:

- Add tests for new business rules.
- Test important validation and failure scenarios.
- Test service behaviour independently where appropriate.
- Test persistence behaviour when database interactions change.
- Cover authorization and access-control requirements where relevant.
- Avoid relying on a developer's existing local database.

### PWA frontend checks

From `frontend-pwa/`:

```bash
npm run typecheck
npm run build
```

### Before submitting

Run the checks relevant to the code you changed.

For contributions affecting multiple components, run the backend and frontend checks as appropriate.

Do not submit a contribution with knowingly failing tests.

---

## Database Changes and Migrations

Matibabu uses SQLite for local facility-node persistence and Flyway for schema management.

Database changes must be introduced through Flyway migrations.

Migration files are located at:

```text
backend/src/main/resources/db/migration/
```

### Migration guidelines

- Never rely on Hibernate schema auto-generation to introduce production schema changes.
- Add a new Flyway migration instead of modifying an already-applied migration.
- Keep migrations focused and understandable.
- Consider existing data when changing tables or constraints.
- Ensure the application remains compatible with the resulting schema.
- Test database changes against the project's configured test environment.

Hibernate is configured to validate the schema rather than automatically update it.

This means entity changes and migration changes must remain consistent.

---

## API Development

Matibabu exposes its backend functionality through REST APIs.

The curated OpenAPI specification is maintained at:

```text
docs/api/openapi.yaml
```

Runnable HTTP examples are maintained in:

```text
requests.http
```

When adding or changing an endpoint:

- Follow existing API conventions.
- Use appropriate request and response DTOs.
- Validate incoming data.
- Return meaningful HTTP status codes.
- Handle expected errors consistently.
- Enforce authentication and authorization requirements.
- Update the