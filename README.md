# Notes Application — Spring Boot

A production-ready RESTful Notes API built with Spring Boot 3, JWT security, Flyway migrations, and full test coverage.

---

## Project Structure

```
notes-app/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/notes/app/
    │   │   ├── NotesApplication.java          # Entry point
    │   │   ├── config/
    │   │   │   ├── JpaConfig.java             # Enables JPA auditing (@CreatedDate etc.)
    │   │   │   └── SecurityConfig.java        # JWT filter chain, method security
    │   │   ├── controller/
    │   │   │   ├── AuthController.java        # POST /api/v1/auth/login
    │   │   │   └── NoteController.java        # CRUD endpoints
    │   │   ├── dto/
    │   │   │   └── NoteDto.java               # Request + Response (entity never exposed)
    │   │   ├── entity/
    │   │   │   └── Note.java                  # JPA entity with auditing
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── NoteNotFoundException.java
    │   │   ├── repository/
    │   │   │   └── NoteRepository.java        # JPA + title search
    │   │   ├── security/
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   └── JwtUtils.java
    │   │   └── service/
    │   │       └── NoteService.java
    │   └── resources/
    │       ├── application.properties
    │       └── db/migration/
    │           └── V1__create_notes_table.sql
    └── test/
        ├── java/com/notes/app/
        │   ├── controller/
        │   │   └── NoteControllerTest.java    # @WebMvcTest slice tests
        │   └── service/
        │       └── NoteServiceTest.java       # Pure unit tests with Mockito
        └── resources/
            └── application-test.properties   # H2 in-memory, Flyway disabled
```

---

## Architecture — Layered Design

```
HTTP Request
    │
    ▼
Controller        ← validates input, maps to/from DTOs, sets HTTP status
    │
    ▼
Service           ← business logic, @Transactional, @PreAuthorize, throws domain exceptions
    │
    ▼
Repository        ← Spring Data JPA, derived queries
    │
    ▼
Database (MySQL)
```

---

## API Endpoints

### Auth
| Method | Path                  | Auth | Description       |
|--------|-----------------------|------|-------------------|
| POST   | /api/v1/auth/login    | No   | Get JWT token     |

### Notes
| Method | Path                        | Auth | Description              |
|--------|-----------------------------|------|--------------------------|
| POST   | /api/v1/notes               | Yes  | Create a note            |
| GET    | /api/v1/notes               | Yes  | List all notes           |
| GET    | /api/v1/notes/{id}          | Yes  | Get note by ID           |
| GET    | /api/v1/notes/search?title= | Yes  | Search by title          |
| PUT    | /api/v1/notes/{id}          | Yes  | Update a note            |
| DELETE | /api/v1/notes/{id}          | Yes  | Delete a note            |

### Health
| Method | Path              | Auth | Description     |
|--------|-------------------|------|-----------------|
| GET    | /actuator/health  | No   | Health check    |

---

## Quick Start

### 1. Create MySQL database
```sql
CREATE DATABASE notesdb;
```

### 2. Configure credentials
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=your_password
app.jwt.secret=your-256-bit-secret-at-least-32-chars-long
```

### 3. Run
```bash
mvn spring-boot:run
```
Flyway automatically creates the `notes` table on first start.

### 4. Get a token
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password"}'
```

### 5. Use the token
```bash
TOKEN="<paste token here>"

# Create a note
curl -X POST http://localhost:8080/api/v1/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"My First Note","content":"Hello, Notes!"}'

# List all notes
curl http://localhost:8080/api/v1/notes \
  -H "Authorization: Bearer $TOKEN"

# Search by title
curl "http://localhost:8080/api/v1/notes/search?title=first" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Running Tests
```bash
mvn test
```
Tests use an H2 in-memory database. Flyway is disabled for tests. All tests are isolated via `@WebMvcTest` (controller slice) and plain Mockito (service unit tests).

---

## Security Design

- **Stateless JWT** — no server-side sessions
- **BCrypt** password hashing
- **`/actuator/health`** and **`/api/v1/auth/**`** are public; everything else requires a valid Bearer token
- **`@PreAuthorize("isAuthenticated()")`** on mutating service methods (method-level security via `@EnableMethodSecurity`)
- **`ownerUsername`** field on `Note` entity is ready for per-user data isolation — wire it up by filtering repository queries by the authenticated principal

---

## Extension Points

| Feature | Where to add |
|---|---|
| User registration | `AuthController` + new `User` entity + `UserRepository` |
| Per-user note isolation | `NoteRepository.findByOwnerUsername()` + filter in `NoteService.getAll()` |
| Pagination | Replace `findAll()` with `findAll(Pageable)` |
| Role-based access | Add `hasRole('ADMIN')` to `@PreAuthorize` |
| Refresh tokens | Add `/api/v1/auth/refresh` + store refresh token in DB |
