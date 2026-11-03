# Database migrations and compatibility

## Scope

Matibabu supports two database engines in the current deployment model:

- SQLite for a facility's local/offline node.
- PostgreSQL for the containerized production/integration deployment.

Flyway migrations are selected by Spring profile. The application must never scan both vendor directories at the same time because V9 exists once per vendor and Flyway requires migration versions to be unique.

## Migration layout

```text
backend/src/main/resources/db/migration/
├── common/
│   ├── V1__...
│   ├── V2__...
│   ├── ...
│   └── V26__...
├── sqlite/
│   ├── V9__initial_schema_for_clinicians_and_sessions.sql
│   └── V26__fix_legacy_clinician_id_type.sql
└── postgresql/
    ├── V9__initial_schema_for_clinicians_and_sessions.sql
    └── V26__fix_legacy_clinician_id_type.sql
```

The shared migrations are identical for both engines. Vendor directories contain only migrations whose SQL or compatibility behavior differs by engine.

## Why V9 remains vendor-specific

The legacy `clinicians.id` `BLOB` problem was corrected by V26 for existing SQLite databases. It was not the only binary type in V9.

V9 also creates Spring Session's `SPRING_SESSION_ATTRIBUTES.ATTRIBUTE_BYTES` column. Because Spring Session JDBC remains planned, that table remains part of the schema:

```sql
-- SQLite V9
ATTRIBUTE_BYTES BLOB NOT NULL
```

```sql
-- PostgreSQL V9
ATTRIBUTE_BYTES BYTEA NOT NULL
```

SQLite accepts `BLOB`; PostgreSQL requires `BYTEA`. The SQLite V9 file therefore remains byte-stable for databases that already applied it, while PostgreSQL receives an equivalent V9 with the PostgreSQL-native type.

If the team later decides that Spring Session JDBC will never be used, the session tables can be removed through a separately reviewed migration strategy. Until that decision is made, the vendor split is the least destructive option and must remain.

## Profile selection

### Local SQLite profile

`application-local.properties` selects:

```properties
spring.flyway.locations=classpath:db/migration/common,classpath:db/migration/sqlite
```

### Production PostgreSQL profile

`application-prod.properties` selects:

```properties
spring.flyway.locations=classpath:db/migration/common,classpath:db/migration/postgresql
```

### Tests

The test configuration selects the SQLite set only:

```properties
spring.flyway.locations=classpath:db/migration/common,classpath:db/migration/sqlite
```

A recursive `classpath:db/migration` location must not be used because it discovers both V9 files and fails with duplicate migration-version errors.

## Legacy SQLite database reconciliation

The included `backend/matibabu-local.db` had already applied V9 using an older byte representation. Its Flyway history recorded checksum `-1355569998`, while the preserved current SQLite V9 resolved to `529705142`.

The database was backed up and the V9 Flyway history checksum was repaired as a metadata-only reconciliation. V9 was not rerun and no domain data was transformed.

The existing database also declared `clinicians.id` as `BLOB`, although its stored clinician UUID was already text with length 36. SQLite V26 rebuilds the table with `id CHAR(36)` and copies the existing values. PostgreSQL V26 is a no-op because PostgreSQL V9 already creates the correct `CHAR(36)` declaration.

The repair and V26 migration were verified by a successful existing-database startup:

- Flyway validated 26 migrations.
- The existing SQLite database reached V26.
- Hibernate schema validation passed.
- The backend started on port 8080.

Do not run `flyway repair` against another database without first backing it up and confirming that the recorded migration was logically equivalent to the current file.

## Containerized verification

The Compose PostgreSQL service uses the `prod` profile. The backend receives the PostgreSQL URL through the Compose service name, not `localhost`:

```text
jdbc:postgresql://postgres:5432/<database>
```

The verified PostgreSQL path applies V1 through V26, validates Hibernate against PostgreSQL 16, and starts the backend successfully. The SQLite path is verified with both a fresh isolated test database and the existing local database.

## Synchronization scope

This migration work does not implement synchronization. V22's outbox remains a common schema migration for both engines. Synchronization remains governed by ADR-08 and requires separate decisions about sequence generation, delete/tombstone semantics, retry state, reference-data direction, facility identity, and transactional outbox writers.

The vendor-specific migration layout does not change outbox behavior. It only allows database-native DDL where required and prevents one engine's migration syntax from blocking the other.

## Verification commands

From `backend/`:

```bash
# Isolated SQLite tests
./mvnw clean test -Dspring.profiles.active=test

# Local application against the existing SQLite database
./mvnw spring-boot:run
```

From the repository root:

```bash
# PostgreSQL-backed production-profile deployment
docker compose up --build -d postgres backend
docker compose logs --tail=200 backend
```

Expected Flyway state after the completed compatibility work is version `26` for both engines.
