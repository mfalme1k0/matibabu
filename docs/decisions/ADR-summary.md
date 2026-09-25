# Matibabu Architecture Decision Records

## ADR-001: Use Clean Architecture with Feature-Oriented Packaging

### Status

Accepted

### Decision

Matibabu will use Clean Architecture principles combined with feature-oriented package organization.

The major architectural layers are:

```text
api
application
domain
infrastructure
config
```

Features are grouped within those layers.

### Rationale

A purely layer-oriented structure such as:

```text
controller/
service/
repository/
```

would become difficult to navigate as the project grows.

Feature-oriented organization allows related functionality to remain conceptually grouped while maintaining architectural boundaries.

### Consequences

Developers should organize new functionality consistently with the established structure.

---

## ADR-002: Keep the Domain Independent of Frameworks

### Status

Accepted

### Decision

The domain and application layers should not depend directly on Spring, JPA, Hibernate, HTTP, or database implementations.

### Rationale

This keeps business logic testable and allows infrastructure technologies to change.

### Consequences

Spring dependency injection should primarily be handled at the composition/configuration boundary.

---

## ADR-003: Use Repository Abstractions

### Status

Accepted

### Decision

The domain defines repository abstractions while infrastructure provides implementations.

Example:

```text
domain.PatientRepository
        ▲
        │
infrastructure.PatientRepositoryAdapter
```

### Rationale

The domain should not know how persistence works.

### Consequences

Database changes should not require rewriting domain logic.

---

## ADR-004: Use UUID v7 for Entity Identity

### Status

Accepted

### Decision

Matibabu will use UUID v7 identifiers for persisted entities where globally unique identifiers are appropriate.

### Rationale

UUID v7 provides UUID-style global uniqueness while incorporating time-ordering characteristics that are beneficial for database indexing and insertion patterns compared with purely random UUIDs.

### Consequences

The identifier generation strategy becomes part of the application's identity policy.

Database mappings must preserve the UUID value correctly.

---

## ADR-005: Use DTOs at the API Boundary

### Status

Accepted

### Decision

API requests and responses will use explicit DTOs.

The domain model will not be directly exposed through HTTP.

### Example

```text
RegisterPatientRequest
        ↓
Patient
        ↓
PatientResponse
```

### Rationale

API contracts and domain models have different responsibilities and may evolve independently.

---

## ADR-006: Centralize Spring Composition

### Status

Accepted

### Decision

Application services should remain framework-independent where practical.

Spring bean composition will be performed through configuration classes under:

```text
config/
```

### Rationale

This creates an explicit composition root and avoids unnecessarily coupling application services to Spring.

---

## ADR-007: Global API Exception Translation

### Status

Accepted

### Decision

Application exceptions will be translated into HTTP responses by shared API exception handling.

For example:

```text
PatientNotFoundException
        ↓
GlobalExceptionHandler
        ↓
404 Not Found
```

### Rationale

The application layer should not need to know about HTTP status codes.

---

## ADR-008: Encounter Is a First-Class Domain Concept

### Status

Accepted

### Decision

An Encounter will be modeled independently from Patient.

An encounter represents a specific episode of care beginning when a patient presents for care and ending when that episode is discharged/closed.

Conceptually:

```text
Patient
   │
   ├── Encounter
   ├── Encounter
   └── Encounter
```

### Rationale

A patient may have many episodes of care over time.

Clinical information, treatment, observations, diagnoses, and discharge information frequently require the context of a specific episode rather than merely the patient identity.

### Consequences

Encounter must have its own identity and lifecycle.

The detailed encounter state machine must be designed before implementation.

Medical records, DHIS2 integration, and synchronization must account for encounter context where appropriate.

---


