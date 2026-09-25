# ADR: Departments

* **Status:** Accepted
* **Date:** 2026-09-25

## Context

A health facility in Matibabu is organized into functional units such as an
outpatient department, a maternity ward, or a laboratory. Encounters,
staffing, and reporting are frequently scoped to one of these units rather
than to the facility as a whole.

Until now, the facility was the only organizational concept in the domain.
There was no way to:

* Group encounters by the unit of the facility where care was delivered.
* Enforce that an organizational unit belongs to exactly one facility.
* Identify a unit consistently across a facility using a short, human-chosen
  code (e.g. `OPD`) in addition to its generated identifier.
* Retire a unit without deleting its history.

A `Department` concept was introduced to address this, following the same
clean-architecture layering used by the rest of the backend (API,
application, domain, infrastructure).

## Decision

We introduced `Department` as a domain concept that belongs to a single
`Facility`.

```text
Facility
   │
   └── Department
          ├── code   (unique within the facility)
          ├── name
          └── active
```

### Domain

`Department` is a domain class with:

* An `id` (UUID), generated with the project's time-ordered UUID strategy.
* A `facilityId`, required at creation and immutable afterwards.
* A `code`, unique per facility, used as a short human-readable identifier
  (e.g. `OPD`).
* A `name`.
* An `active` flag, `true` by default.

It exposes behavior rather than plain setters:

* `create(facilityId, code, name)` — factory for a new department.
* `reconstitute(...)` — factory used when rebuilding a department from
  persistence.
* `rename(name)` and `changeCode(code)`.
* `deactivate()` and `reactivate()`.

Code and name are validated as non-blank at construction and on every
change; a missing facility reference is rejected with a
`NullPointerException` from `Objects.requireNonNull`.

### Repository Abstraction

`DepartmentRepository` is defined in the domain layer:

```text
save(department)
findById(id)
findByFacilityId(facilityId)
findAllActive(facilityId)
findByFacilityIdAndCode(facilityId, code)
existsByFacilityIdAndCode(facilityId, code)
```

`DepartmentRepositoryAdapter` implements it in infrastructure, backed by
`SpringDataDepartmentsRepository` (Spring Data JPA) and `DepartmentEntity`,
with `DepartmentMapper` (MapStruct) converting between the entity and the
domain model.

### Application Layer

Three use cases coordinate department operations:

* `CreateDepartmentUseCase` — creates a department for a facility. The
  service verifies the facility exists (`FacilityNotFoundException` if not)
  and that the code is not already used within that facility
  (`DepartmentCodeAlreadyExistsException` if it is).
* `ListDepartmentUseCase` — retrieves a single department by id
  (`DepartmentNotFoundException` if missing), all departments for a
  facility, or only the active ones for a facility. Facility-scoped queries
  first confirm the facility exists.
* `DeactivateDepartmentUseCase` — loads a department, deactivates it, and
  persists the change.

### API Layer

A REST endpoint was introduced under:

```text
POST /api/departments
```

`CreateDepartmentRequest` carries `facilityId`, `code`, and `name`;
`DepartmentResponse` reflects the created department's id, facility id,
code, name, and active status. The listing and deactivation use cases exist
in the application layer but are not yet exposed as endpoints.

### Persistence

The `departments` table (migration `V23`) stores `id`, `facility_id`,
`code`, `name`, and `active`, with a foreign key to `facilities`, an index
on `facility_id`, and a unique index on `(facility_id, code)` so uniqueness
is enforced at the database level as well as in the application layer.

A follow-up migration (`V24`) adds a nullable `department_id` column and
index to `encounters`, allowing an encounter to optionally record the
department where care took place.

## Rationale

Modeling departments as a first-class concept, owned by a facility, mirrors
how the earlier encounter/facility work in this project scopes clinical
concepts to their organizational context (see ADR-05 and ADR-07). Keeping
`code` unique per facility (rather than globally) allows different
facilities to reuse the same short codes, which matches how departments are
named in practice.

Deactivation was chosen over deletion so that historical references (e.g.
from encounters) remain valid, consistent with how other reference data in
the project is retired rather than removed.

Linking `department_id` on `encounters` as a separate, additive migration
keeps the encounter schema backward compatible and avoids retrofitting
existing encounters with a required department.

## Consequences

### Positive

* Encounters and future reporting can be scoped to a specific department.
* Facility-scoped uniqueness on `code` is enforced by both the application
  and the database.
* Departments can be retired via `deactivate()`/`reactivate()` without
  losing history.
* The design follows the project's established layering, so it is
  consistent with other features to read and extend.

### Trade-offs

* Only department creation is currently exposed over the API; listing and
  deactivation have application services but no endpoints yet.
* `department_id` on `encounters` is optional, so nothing currently
  enforces that an encounter records a department.
* `CreateDepartmentUseCase.Create` and `DeactivateDepartmentUseCase.deactivate`
  use non-standard parameter/method capitalization (`Create`, `Id`)
  inconsistent with the rest of the codebase's Java conventions.

## Implementation

The functionality introduced includes:

* `Department` domain model
* `DepartmentRepository` abstraction and `DepartmentRepositoryAdapter`
* `CreateDepartmentUseCase` / `CreateDepartmentService`
* `ListDepartmentUseCase` / `ListDepartmentService`
* `DeactivateDepartmentUseCase` / `DeactivateDepartmentService`
* `DepartmentController` with `POST /api/departments`
* `DepartmentCodeAlreadyExistsException`, `DepartmentNotFoundException`
* `DepartmentEntity`, `DepartmentMapper`, `SpringDataDepartmentsRepository`
* Flyway migrations `V23` (`departments` table) and `V24`
  (`encounters.department_id`)

## Status

**Accepted and implemented.**