# Seeding the `facilities` table — brief

## Schema

Defined in `V19__create_facilities_table.sql`:

| column | notes |
|---|---|
| `id` | UUID, time-ordered (`UuidCreator.getTimeOrderedEpoch()` in code; for a raw SQL seed, any valid UUID works) |
| `name` | required |
| `type` | one of `DISPENSARY`, `HEALTH_CENTRE`, `SUB_COUNTY_HOSPITAL`, `COUNTY_REFERRAL_HOSPITAL`, `NATIONAL_REFERRAL_HOSPITAL`, `OTHER` — see `FacilityType` enum |
| `mfl_code` | optional, but **unique** if present (`ix_facilities_mfl_code`) — this is Kenya's official Master Facility List code |
| `active` | defaults `TRUE` |

## Source data

Use the Kenya Master Health Facility List (KMHFL/MFL) — not a hand-typed list. These `mfl_code` values are meant to be real national registry codes, and other parts of the system (e.g. referrals) may eventually rely on them being genuine.

- **API**: `https://api.mfltest.ehealth.or.ke`, documented at `mfl-api-docs.readthedocs.io`. May require an API key or account — check before assuming access.
- **Open snapshot (easier starting point)**: a 2020 KMHFL export is published on openAfrica (`bulk.openafrica.net`), under "Kenya Master Health Facility List." CKAN-based, downloadable, Creative Commons licensed. It will be stale relative to the live registry, so treat it as a dev/seed dataset rather than an authoritative source.
- **Type mapping**: KMHFL's facility-type categories don't map 1:1 onto our six-value `FacilityType` enum. That mapping needs a deliberate decision (documented somewhere, e.g. a short ADR or a comment in the seed script) rather than a best guess made in passing.

## How to load it

Same pattern already used for the medicines seed:

1. **Flyway migration** — good for a small curated starter set. Next free version number (check `backend/src/main/resources/db/migration/` for the current highest `V*`). Plain `INSERT`s, same style as `V13__seed_clinical_data_table.sql`.
2. **A generator script** — mirror `backend/tools/match_keml_to_atc.py` (used for the medicines/ATC case) once the dataset is bigger than a handful of rows. Pull the KMHFL export, map type codes, emit the migration SQL. Worth doing this way once you're past ~20–30 rows so the list isn't hand-maintained.

## Things to watch for

- **`mfl_code` uniqueness** — the export may contain duplicates or blanks. Dedupe or null them out before inserting, or the migration will fail on the unique index.
- **Application-layer duplicate check** — if seeding through the running API instead of a migration, `CreateFacilityService` already throws `DuplicateMflCodeException` on a clash; no extra handling needed there.
- **No role restriction today** — `POST /api/facilities` has no admin-only lock right now (unlike `/api/admin/*`), so any authenticated user can create facilities via the live API. Not a seeding blocker, just worth knowing if this ends up seeded through the API rather than a migration.
- **Departments depend on facilities** — `departments.facility_id` (`V23__create_departments_table.sql`) references `facilities.id`. If departments are being seeded too, facilities need to exist first.