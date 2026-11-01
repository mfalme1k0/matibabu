# Departments and encounters API

This document describes the HTTP surface and domain boundary rules for
**departments** and **encounters**. Full schemas live in [openapi.yaml](./openapi.yaml).

## Concepts

- A **department** belongs to exactly one **facility** and has a stable `code`
  unique within that facility.
- An **encounter** is attributed to a facility and a department.
- On a facility node, `facilityId` for new encounters is taken from
  **`NodeIdentity`** (`app.facility.id`), never from the client body.
- The attending clinician is taken from the **authenticated principal**.

## Department endpoints

Base path: `/api/departments`  
Auth: authenticated clinician; CSRF required on mutating methods.

| Method | Path | Purpose | Success |
|--------|------|---------|---------|
| `POST` | `/api/departments` | Create department | **201** |
| `GET` | `/api/departments/{id}` | Get by id | **200** |
| `GET` | `/api/departments?facilityId={uuid}&activeOnly={bool}` | List for facility | **200** |
| `POST` | `/api/departments/{id}/deactivate` | Deactivate | **200** |

### Create body

```json
{
  "facilityId": "00000000-0000-4000-8000-000000000001",
  "code": "OPD",
  "name": "Outpatient Department"
}
```

### Response shape

```json
{
  "id": "…",
  "facilityId": "…",
  "code": "OPD",
  "name": "Outpatient Department",
  "active": true
}
```

### Errors

| Situation | Status | Exception |
|-----------|--------|-----------|
| Facility does not exist | **404** | `FacilityNotFoundException` |
| Duplicate code for facility | **409** | `DepartmentCodeAlreadyExistsException` |
| Department not found | **404** | `DepartmentNotFoundException` |
| Validation (blank code/name) | **400** | Bean validation |

Local development facility id (see `application-local.properties`):

`00000000-0000-4000-8000-000000000001`

## Encounter endpoints

Base path: `/api/encounters`

| Method | Path | Purpose | Success |
|--------|------|---------|---------|
| `POST` | `/api/encounters` | Start encounter | **201** |
| `GET` | `/api/encounters/{id}` | Get by id | **200** |
| `POST` | `/api/encounters/{id}/discharge` | Discharge | **200** |
| `POST` | `/api/encounters/{id}/cancel` | Cancel | **200** |

### Start body

```json
{
  "patientId": "…",
  "departmentId": "…"
}
```

Do **not** send `facilityId` or `attendingClinicianId` in the body.

### Start response (includes attribution)

```json
{
  "id": "…",
  "patientId": "…",
  "attendingClinicianId": "…",
  "facilityId": "…",
  "departmentId": "…",
  "startedAt": "…",
  "status": "ACTIVE",
  "endedAt": null
}
```

### Start boundary rules (`StartEncounterService`)

1. Load department by `departmentId` → missing → **404** `DepartmentNotFoundException`.
2. Compare `department.facilityId` to `NodeIdentity.facilityId()` → mismatch → **400** `DepartmentFacilityMismatchException`.
3. Require `department.active == true` → inactive → **400** `DepartmentNotActiveException`.
4. Create encounter with server-side facility id and clinician id; persist.

### Lifecycle errors

| Situation | Status | Exception |
|-----------|--------|-----------|
| Encounter not found | **404** | `EncounterNotFoundException` |
| Discharge/cancel when not `ACTIVE` | **409** | `EncounterNotActiveException` |

Status machine:

```text
ACTIVE ──discharge()──► DISCHARGED
   └──cancel()─────────► CANCELLED
```

## Related ADRs

- [ADR-09 Departments](../decisions/ADR-09-departments.md)
- [ADR-01 Encounters](../decisions/ADR-01-Encounters.md)
- [ADR-08 Synchronization](../decisions/ADR-08-synchronization.md) (facility node identity)
