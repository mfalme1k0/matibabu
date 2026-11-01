# Matibabu API documentation

| Resource | Description |
|----------|-------------|
| [openapi.yaml](./openapi.yaml) | OpenAPI 3.0.3 contract (Patients, Departments, Encounters) |
| [patient-registry.md](./patient-registry.md) | Narrative notes for the patient registry |
| [departments-and-encounters.md](./departments-and-encounters.md) | Department + encounter HTTP API and boundary rules |
| `../../requests.http` | Executable HTTP examples (IDE REST client) |

## Interactive docs (Swagger UI)

With the backend running locally:

| URL | Content |
|-----|---------|
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/openapi.yaml | Curated OpenAPI YAML (default in UI) |
| http://localhost:8080/v3/api-docs | springdoc-generated document from controllers |

Swagger UI loads a CSRF/session helper (`/swagger-csrf-helper.js`) so **Try it out** can send `JSESSIONID` and `X-XSRF-TOKEN`.

### Try it out authentication

The API uses **form login + HTTP session** and **cookie CSRF** (not Bearer tokens).

1. Open Swagger UI (the helper calls `GET /csrf` and seeds the `XSRF-TOKEN` cookie).
2. Login in the **same browser** via `POST /login` with form fields `email` and `password`.  
   Local seed admin (Flyway `V10`): `admin@example.com` / `Password`.
3. Mutating requests then include the session cookie and `X-XSRF-TOKEN` automatically.

Without a session → **401**. Mutating call without CSRF → **403**.

### Keeping the curated spec in sync

Edit `docs/api/openapi.yaml`, then copy into the runtime static resource:

```bash
cp docs/api/openapi.yaml backend/src/main/resources/static/openapi.yaml
```

The curated file is the source of truth for Swagger’s default definition; `/v3/api-docs` is useful to spot controller drift.
