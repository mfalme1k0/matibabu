# Matibabu

**Matibabu** is an offline-first Electronic Medical Records (EMR) platform designed for healthcare facilities operating in environments where network connectivity cannot be assumed.

The system is built around a local-first clinical workflow: clinical data is persisted locally, clinical operations remain independent of continuous connectivity, and synchronization with central systems is treated as a separate architectural concern.

---

## Current Status

Matibabu is under active development.

The backend currently provides working clinical and facility functionality, local persistence, authentication and authorization, database migrations, automated testing, and the architectural foundation for offline synchronization.

The facility model now supports **departmentalization**, allowing clinical encounters to be attributed to a specific department within a facility.

Current development is moving toward:

* Facility and department-level reporting
* Continued synchronization implementation
* Remote data exchange
* DHIS2 interoperability

---

## Current Capabilities

The backend currently contains functionality for:

* Patient management
* Clinical encounters
* Encounter lifecycle management
* Facility management
* Department management
* Facility and department association
* Medical records
* Clinical observations
* Diagnoses
* Vitals
* Treatments
* Medicines
* ATC mapping and review
* Referrals
* Clinician authentication
* Role-based security
* CSRF protection
* Local SQLite persistence
* Flyway database migrations
* Repository adapters
* Automated testing
* Initial offline synchronization architecture

---

---

# Local development

## Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

Default local profile uses SQLite and a fixed facility id
(`app.facility.id=00000000-0000-4000-8000-000000000001` in `application-local.properties`).

Seed admin (Flyway): `admin@example.com` / `Password`.

## API documentation

| Resource | URL / path |
|----------|------------|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Curated OpenAPI YAML | http://localhost:8080/openapi.yaml |
| Generated OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Contract source | [docs/api/openapi.yaml](docs/api/openapi.yaml) |
| HTTP examples | [requests.http](requests.http) |
| Narrative API notes | [docs/api/](docs/api/) |

Swagger UI is public. Clinical endpoints still require a session. The UI loads
`/swagger-csrf-helper.js` so **Try it out** sends `JSESSIONID` and `X-XSRF-TOKEN`
after you log in in the same browser (`POST /login` with form fields `email` and
`password`).

CSRF bootstrap: `GET /csrf` (also invoked automatically by the Swagger helper).

## Stack notes

* Spring Boot 4.x, Java 25 (toolchain as declared in `backend/pom.xml`)
* springdoc-openapi **3.x** (`springdoc-openapi-starter-webmvc-ui`) for Swagger UI
* Security: form login, session cookie, cookie CSRF (`XSRF-TOKEN` / `X-XSRF-TOKEN`)


# Architecture

The backend follows a domain-oriented architecture that separates business rules, application orchestration, infrastructure, security, and external-system concerns.

```text
                         API
                          │
                          ▼
                  Application Layer
                  Use Cases / Services
                          │
                          ▼
                     Domain Layer
               Business Rules / Interfaces
                          ▲
                          │
                          │ implementations
                          │
                 Infrastructure Layer
              Persistence / External Systems
                          │
                          ▼
                       SQLite
```

The architectural goal is to keep clinical and operational business rules independent of persistence technology, framework concerns, and external health-information systems.

---

# Repository Structure

```text
matibabu/
├── backend/
├── frontend/
├── database/
├── docs/
├── requests.http
└── README.md
```

The backend is currently the primary implementation area.

```text
backend/src/main/java/com/matibabu/backend/

├── api/
├── application/
├── domain/
├── infrastructure/
├── security/
├── config/
└── exception/
```

Domain capabilities are organized around clinical and operational concepts rather than being implemented as one large collection of framework-specific classes.

---

# Domain Model

Matibabu models healthcare operations as separate domain concepts with explicit boundaries.

The current organizational relationship is:

```text
                    Facility
                       │
             ┌─────────┴─────────┐
             │                   │
        Department A        Department B
             │                   │
             ▼                   ▼
        Encounters           Encounters
             │
             ▼
          Patients
```

A department belongs to exactly one facility.

Clinical activity references the department responsible for the activity rather than making the department responsible for owning clinical records.

This keeps organizational structure separate from clinical aggregates while still allowing clinical activity to be attributed to a facility and department.

---

# Clinical Domains

## Patients

The patient domain currently supports:

* Patient registration
* Patient retrieval
* Patient listing
* Patient updates
* Patient deletion
* Phone-number search
* Validation
* Duplicate phone-number detection
* UUID-based patient identity
* Identity-document information
* Facility association

Patient behaviour is represented in the domain model, while persistence is handled through repository interfaces and infrastructure adapters.

Patient identity and timestamps also form part of the current synchronization design.

---

## Encounters

Encounters represent clinical interactions involving a patient.

The current implementation supports:

* Starting encounters
* Recording the attending clinician
* Recording the facility
* Recording the department
* Encounter status
* Discharging encounters
* Cancelling encounters
* Retrieving encounters
* Persistence through repository adapters

An encounter currently records:

```text
Encounter
├── patientId
├── attendingClinicianId
├── facilityId
├── departmentId
├── startedAt
├── status
└── endedAt
```

The encounter lifecycle is governed by domain rules.

For example:

```text
ACTIVE
  │
  ├── discharge() ──► DISCHARGED
  │
  └── cancel() ─────► CANCELLED
```

The attending clinician is recorded explicitly on the encounter rather than inferred from persistence or authentication state.

The facility is obtained from the local node identity when an encounter is started. The requested department is validated against that facility before the encounter is created.

This ensures that:

```text
Encounter.departmentId
        │
        ▼
Department.facilityId
        │
        ▼
Current NodeIdentity.facilityId
```

must represent the same facility boundary when creating a new encounter.

---

# Medical Records

The medical-record domain currently supports:

* Creating medical records
* Retrieving medical records
* Listing a patient's medical records
* Clinical observations
* Diagnoses
* Vitals
* Treatments

Medical records are associated with clinical encounters and patients.

The persistence implementation separates the medical-record aggregate from its database representation.

Treatment persistence is currently handled as part of the medical-record persistence boundary, with treatments re-synchronized in full when the medical record is saved.

---

# Medicines

Matibabu contains a medicine domain and supporting persistence functionality.

Current functionality includes:

* Medicine retrieval
* Medicine listing
* ATC classification metadata
* Identification of unresolved ATC mappings
* ATC mapping review
* ATC mapping resolution
* Mapping audit metadata

The medicine catalogue is kept separate from patient clinical records.

---

# Referrals

Referrals are represented as an independent domain concept with an explicit lifecycle.

The current implementation includes:

* Referral creation
* Referral retrieval
* Referral cancellation
* Referral completion
* Referral status
* Referral urgency
* Referring clinician
* Originating encounter
* Patient
* Diagnosis information
* Referral reason
* Receiving facility
* Receiving department information

Referral lifecycle transitions are handled through application services and domain rules.

---

# Facilities

Facility management is implemented as a distinct domain.

The backend contains:

```text
api/facility/
application/facility/
domain/facility/
infrastructure/persistence/facility/
```

The facility domain includes:

* Facility creation
* Facility listing
* Facility deactivation
* Facility type
* MFL code
* Facility identity
* Facility association with clinical data

The persistence boundary includes:

```text
Facility
FacilityRepository
FacilityEntity
FacilityMapper
FacilityRepositoryAdapter
SpringDataFacilitiesRepository
```

---

# Departments

Departments are modeled as a **first-class domain concept** rather than as a collection owned by the Facility aggregate.

The department model includes:

* Department identity
* Facility ownership
* Stable department code
* Department name
* Active/inactive state

Conceptually:

```text
Facility
   │
   ├── Department
   │      ├── code
   │      ├── name
   │      └── active
   │
   └── Department
          ├── code
          ├── name
          └── active
```

A department does not own collections of encounters, patients, or other clinical records.

Instead, other domains reference the department through its identifier.

This keeps departmentalization an organizational concern while allowing clinical data to be attributed to a department.

### Facility boundary

Department ownership is enforced at the application boundary in `StartEncounterService`.

When an encounter is started:

1. The current facility is obtained from `NodeIdentity` (`app.facility.id`).
2. The requested department is loaded (`DepartmentNotFoundException` → 404 if missing).
3. The department's `facilityId` is compared with the current facility
   (`DepartmentFacilityMismatchException` → 400 on mismatch).
4. The department must be **active** (`DepartmentNotActiveException` → 400 if deactivated).
5. The encounter is created with server-side `facilityId` and the authenticated clinician id.

This prevents a local facility from creating clinical activity against a department belonging to another facility, and blocks use of retired departments.

### Department HTTP API

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/api/departments` | Create |
| `GET` | `/api/departments/{id}` | Get by id |
| `GET` | `/api/departments?facilityId=&activeOnly=` | List |
| `POST` | `/api/departments/{id}/deactivate` | Deactivate |

See [docs/api/departments-and-encounters.md](docs/api/departments-and-encounters.md) and [docs/api/openapi.yaml](docs/api/openapi.yaml).

---

# Security

Authentication and authorization are implemented using Spring Security.

The security boundary currently includes:

```text
security/
├── controllers/
│   ├── AdminController
│   ├── AuthController
│   ├── CsrfController
│   └── DemoController
│
├── CustomUserDetails.java
├── CustomUserDetailsService.java
├── SecurityConfig.java
│
├── entity/
│   ├── Clinician.java
│   └── Role.java
│
├── repository/
│   └── ClinicianRepository.java
│
└── services/
    ├── ClinicianService.java
    └── ClinicianServiceImpl.java
```

Security is kept outside the clinical domain.

CSRF protection is explicitly configured as part of the web security boundary
(`CookieCsrfTokenRepository`, header `X-XSRF-TOKEN`).

Swagger UI and OpenAPI resources (`/swagger-ui/**`, `/v3/api-docs/**`,
`/openapi.yaml`, `/swagger-csrf-helper.js`) are permit-all. All `/api/**`
clinical routes remain authenticated.

---

# Offline-First Architecture

Offline-first operation is a core architectural requirement.

The system assumes that a facility may need to continue clinical operations when a central service or network connection is unavailable.

The local system therefore acts as the operational environment for clinical activity:

```text
┌───────────────────┐
│   Clinical Client │
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│ Local Application │
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│      SQLite       │
└─────────┬─────────┘
          │
          │ synchronization
          ▼
┌───────────────────┐
│  Central / Remote │
│      System       │
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│      DHIS2        │
└───────────────────┘
```

The local database is currently SQLite:

```text
jdbc:sqlite:./matibabu-local.db
```

This allows the clinical application to operate locally without requiring a continuously available remote database.

---

# Synchronization

The synchronization architecture is being developed independently from the clinical domains.

The backend contains a dedicated `NodeIdentity` component and synchronization-related architectural decisions are documented under:

```text
docs/decisions/
```

The current design establishes an important concept for an offline-first system:

> A local installation is an identifiable node rather than an anonymous database replica.

Conceptually:

```text
                 Matibabu
                    │
             ┌──────┴──────┐
             │             │
          Node A         Node B
             │             │
          SQLite         SQLite
             │             │
             └──────┬──────┘
                    │
                    ▼
             Central System
```

### Currently established

* Node identity
* Facility identity at the local node
* Synchronization-aware patient identity
* Synchronization-aware patient timestamps
* Synchronization architectural documentation
* Persistence decisions that account for synchronization

### Not yet implemented as a complete synchronization subsystem

* General-purpose sync service
* Outbox/change-log processing
* Push/pull protocol
* Sync checkpoints or cursors
* Conflict-resolution engine
* Retry/acknowledgement protocol
* Complete remote synchronization workflow

Synchronization is therefore intentionally being developed incrementally rather than introducing a large synchronization abstraction before the protocol and consistency requirements are established.

---

# Persistence

The current local persistence implementation uses:

* SQLite
* Spring Data JPA
* Hibernate
* Hibernate Community Dialects
* Flyway

The persistence architecture follows repository ports and infrastructure adapters.

```text
Domain Repository
       │
       ▼
Persistence Adapter
       │
       ▼
Spring Data Repository
       │
       ▼
Persistence Entity
       │
       ▼
SQLite
```

MapStruct is used where domain and persistence representations require mapping.

Persistence entities are not treated as the domain model.

---

# Database Migrations

Database schema evolution is managed through Flyway.

Migrations are stored under:

```text
backend/src/main/resources/db/migration/
```

The migration history covers the evolution of the system through:

* Patient details
* Medical records
* Clinical data
* Clinicians and sessions
* Medicines
* ATC metadata
* Referrals
* Facilities
* Facility associations
* Patient identity documents
* Departments
* Encounter department attribution

The facility and departmentalization changes are represented explicitly in the migration history rather than being applied through automatic schema generation.

Database changes are therefore versioned, reviewable, and source controlled.

---

# Application Layer

Application behaviour is represented through use-case interfaces and service implementations.

For example:

```text
StartEncounterUseCase
        │
        ▼
StartEncounterService
        │
        ├── DepartmentRepository
        │
        ├── NodeIdentity
        │
        └── EncounterRepository
```

The application service is responsible for coordinating the workflow:

```text
Request
   │
   ▼
Identify current facility
   │
   ▼
Load department
   │
   ▼
Validate department belongs to facility
   │
   ▼
Create Encounter
   │
   ▼
Persist Encounter
```

The application layer coordinates workflows without becoming the owner of core domain rules.

---

# API

The backend exposes REST endpoints for the implemented clinical and operational capabilities.

Current API areas include:

```text
/api/patients
/api/encounters
/api/facilities
/api/medical-records
/api/medicines
/api/referrals
```

Authentication, administration, and CSRF-related endpoints are handled separately by the security layer.

API request and response models are kept separate from domain objects.

---

# Error Handling

API exception handling is centralized through:

```text
api/exception/GlobalExceptionHandler
```

The application defines specific exceptions for cases including:

* Patient not found
* Encounter not found
* Facility not found
* Medical record not found
* Medicine not found
* Referral not found
* Duplicate phone number
* Duplicate MFL code
* Invalid diagnosis reference
* Invalid referral state
* User not found
* Existing clinician conflicts

Application and domain errors are translated into HTTP responses at the API boundary rather than coupling domain code directly to HTTP concerns.

---

# Testing

Testing is performed at multiple levels.

### Domain tests

Verify domain behaviour and business invariants independently of infrastructure.

### Application tests

Verify application services and use-case orchestration.

Lightweight in-memory repositories are used where they provide a clear isolation boundary. Mockito is used where mocking provides useful service-level isolation.

### Persistence tests

Verify:

* Entity mappings
* Repository adapters
* Database persistence
* Domain/persistence mapping
* Department persistence
* Encounter facility and department attribution

### API and integration tests

Verify behaviour across application boundaries, including:

* HTTP requests
* Validation
* Persistence
* Error handling
* Security integration

### Current verification

The backend currently passes the full Maven test suite:

```bash
./mvnw clean test
```

---

# Technology Stack

| Area                | Technology                           |
| ------------------- | ------------------------------------ |
| Language            | Java 25                              |
| Framework           | Spring Boot 4.1                      |
| Web                 | Spring MVC                           |
| Persistence         | Spring Data JPA                      |
| ORM                 | Hibernate                            |
| Local database      | SQLite                               |
| Database migrations | Flyway                               |
| SQLite support      | Hibernate Community Dialects         |
| Mapping             | MapStruct                            |
| Security            | Spring Security                      |
| Testing             | JUnit 5 / Spring Boot Test / Mockito |
| Build               | Maven Wrapper                        |
| Identifiers         | UUID Creator                         |

---

# Development Workflow

Matibabu generally follows this progression when introducing a capability:

```text
Requirement
     │
     ▼
Domain Model / Invariants
     │
     ▼
Application Use Case
     │
     ▼
Repository Port
     │
     ▼
Infrastructure Adapter
     │
     ▼
Database Migration
     │
     ▼
API
     │
     ▼
Tests
```

Architecturally significant decisions are documented through ADRs.

ADRs are reserved for decisions with meaningful long-term architectural consequences rather than every implementation detail.

---

# Architectural Principles

## Domain independence

Clinical rules should not depend directly on:

* Spring
* JPA
* Hibernate
* SQLite
* Authentication infrastructure
* DHIS2

## Local-first operation

Clinical workflows should remain usable without continuous network connectivity.

## Explicit schema evolution

Database changes are versioned through Flyway migrations.

## Explicit boundaries

Clinical domains, application orchestration, persistence, security, and synchronization should remain independently understandable.

## Organizational boundaries

Facility and department relationships should be represented explicitly rather than inferred from clinical records.

Clinical activity references organizational units through identifiers without making organizational aggregates responsible for owning clinical data.

## External systems at the boundary

Remote systems and DHIS2-specific concerns should be isolated from the core clinical domain wherever practical.

## Incremental architecture

Architectural concepts are introduced as requirements become concrete rather than building large abstractions before they are needed.

---

# Current Development Direction

The current development path is:

```text
Clinical Workflows
       │
       ▼
Facility Model
       │
       ▼
Departmentalization
       │
       ▼
Facility / Department Attribution
       │
       ▼
Facility / Department Reporting
       │
       ▼
Synchronization
       │
       ▼
Remote Infrastructure
       │
       ▼
DHIS2 Interoperability
```

### Current focus

**Facility and department-level reporting.**

The organizational model now establishes:

```text
Facility
   │
   └── Department
          │
          └── Encounter
```

New encounters are associated with a department belonging to the current facility.

The next step is to build reporting and aggregation capabilities on top of this established organizational model.

---

# Roadmap

### Clinical

* Continue expanding clinical workflows
* Strengthen clinical validation and domain invariants

### Facility Management

* Build facility-level reporting
* Build department-level reporting
* Establish reporting queries and aggregation boundaries
* Continue refining facility and department administration

### Synchronization

* Continue the initial synchronization implementation
* Establish change tracking
* Define synchronization protocol
* Implement remote synchronization
* Handle retries and failures
* Define conflict-resolution behaviour

### Interoperability

* Establish reporting mappings
* Integrate with DHIS2
* Separate DHIS2-specific concepts from the clinical domain

---

# Development Setup

## Requirements

* Java 25
* Git
* Maven Wrapper

A system-wide Maven installation is not required.

## Clone

```bash
git clone git@github.com:mfalme1k0/matibabu.git
cd matibabu/backend
```

## Verify Java

```bash
java -version
```

## Run tests

```bash
./mvnw clean test
```

## Run the backend

```bash
./mvnw spring-boot:run
```

The local backend uses SQLite for development.

---

# Documentation

Architectural decisions are maintained under:

```text
docs/
```

Synchronization architecture and other significant architectural decisions are documented as ADRs.

ADRs are intentionally reserved for decisions with meaningful long-term architectural consequences rather than every implementation detail.

---

## License

See [`LICENSE`](LICENSE) for licensing information.

```

This version makes one important change in the project's story: **departmentalization is no longer a roadmap item—it is an implemented architectural capability.** Reporting is now the next layer built on top of it.
```
