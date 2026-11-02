# Matibabu

**Offline-first Electronic Medical Records for low-connectivity clinics**

Matibabu is designed for healthcare facilities where continuous internet connectivity cannot be assumed. Clinical workflows operate locally, while synchronization is handled as a separate concern.

---

### Problem

Healthcare facilities in low-connectivity environments face challenges with systems that depend on constant internet access. Unreliable networks can interrupt clinical workflows and create difficulties maintaining consistent records across facilities.

### Solution

Matibabu is designed around an offline-first approach, allowing clinical operations to continue independently of network availability.

Core capabilities include:

- Patient registration and management
- Clinical encounter management and lifecycle
- Clinical observations, diagnoses, vitals, and treatments
- Patient referrals
- Role-based access control
- Facility and department management
- Local persistence using SQLite
- Architectural groundwork for offline synchronization

---

### Tech Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Security:** Spring Security
- **Persistence:** Spring Data JPA, Hibernate
- **Databases:** SQLite, PostgreSQL
- **Migrations:** Flyway
- **Testing:** JUnit 5, Mockito
- **API Documentation:** OpenAPI, Swagger UI
- **Architecture:** Domain-oriented architecture with application and infrastructure boundaries

---

### Running Locally

#### Prerequisites

- Java 25
- Git
- Maven (or use the included Maven Wrapper)

#### 1. Clone the repository

```bash
git clone https://github.com/mfalme1k0/matibabu.git
cd matibabu/backend
```

#### 2. Start the application

The application activates the `local` Spring profile by default.

The local configuration uses SQLite for persistence, with Flyway managing database migrations during startup.

By default, the database file is created at:

```text
backend/matibabu-local.db
```

Start the application using the Maven Wrapper:

```bash
./mvnw spring-boot:run
```

The application runs on port `8080`.

#### 3. Access the API documentation

Once the application has started:

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Curated OpenAPI specification | http://localhost:8080/openapi.yaml |
| Generated OpenAPI specification | http://localhost:8080/v3/api-docs |

Swagger UI provides an interactive interface for exploring and testing the available API endpoints.

#### 4. Run tests

Execute the test suite:

```bash
./mvnw clean test
```

---

### Current Status

The backend currently supports core clinical workflows, facility and department management, authentication, and the architectural groundwork for offline synchronization.

Active development is focused on:

- Reporting
- Offline synchronization
- Interoperability with external healthcare systems

---

### Project Structure

The backend follows a domain-oriented architecture with separation between application logic, domain concepts, and infrastructure concerns.

See the source directories and project documentation for implementation details.

---

### Contributing

Contributions, discussions, and suggestions are welcome.

If you encounter a bug or have an improvement in mind, feel free to open an issue or submit a pull request.

---

**Matibabu** — Building resilient healthcare software for environments where connectivity cannot be guaranteed.