# Lab 10 – Implementation of HTTP (Spring Boot)

## Objective
Learn secure request processing, input parsing, basic validation, and correct HTTP response handling.  
The project demonstrates how Spring Boot controllers handle:
- HTTP routing (GET/POST)
- Request headers
- JSON request bodies (DTO)
- Form data (MVC)
- Validation + custom validator
- Proper HTTP status codes (200 / 400 / 404 / 415 / 500)
- Global exception handling
- JWT Authentication & Authorization

---

## Requirements
- **Java JDK 21+** (tested with Java 23)
- **Git**
- **Maven** or Maven Wrapper (included: `mvnw`, `mvnw.cmd`)

---

## How to Run

### Windows (PowerShell)
Open PowerShell inside the project root (where `pom.xml` exists) and run:

```powershell
.\mvnw.cmd clean spring-boot:run -DskipTests
```

### macOS / Linux
```bash
./mvnw clean spring-boot:run -DskipTests
```

When the server starts, it runs on:
- **http://localhost:8080**

---

## Database
This project uses **SQLite** as the database. The database file (`database.db`) is created automatically on first run. No external database setup is required.

---

## API Endpoints (Documentation)

### 1) Hello endpoint (basic GET)

| Property | Value |
|----------|-------|
| Method   | GET   |
| Path     | `/hello` |
| Returns  | 200 OK (text/HTML) |

**Test:**
- Browser: http://localhost:8080/hello
- PowerShell:
```powershell
Invoke-WebRequest http://localhost:8080/hello -UseBasicParsing
```

---

### 2) Read request headers

| Property | Value |
|----------|-------|
| Method   | GET   |
| Path     | `/api/headers` |
| Headers  | X-Student, X-Test |
| Returns  | 200 OK (JSON) |

**Test:**
```powershell
Invoke-WebRequest "http://localhost:8080/api/headers" `
  -Headers @{ "X-Student"="Roni"; "X-Test"="123" } `
  -UseBasicParsing
```

---

### 3) Create user (JSON body + validation)

| Property | Value |
|----------|-------|
| Method   | POST  |
| Path     | `/api/users` |
| Content-Type | application/json |

**Request Body (JSON):**
```json
{
  "username": "roni_123",
  "email": "roni@mail.com",
  "password": "SecurePass123!"
}
```

**Responses:**
- Success: `200 OK` (JSON)
- Validation Error: `400 Bad Request` (structured error JSON)
- Wrong Content-Type: `415 Unsupported Media Type`

**Test (Correct JSON):**
```powershell
$body = @{
  username = "roni_123"
  email    = "roni@mail.com"
  password = "SecurePass123!"
} | ConvertTo-Json

Invoke-RestMethod "http://localhost:8080/api/users" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

**Test (Wrong Content-Type → 415):**
```powershell
Invoke-WebRequest "http://localhost:8080/api/users" `
  -Method Post `
  -ContentType "text/plain" `
  -Body "hi" `
  -UseBasicParsing
```

---

### 4) MVC form submit (form data parsing)

| Property | Value |
|----------|-------|
| Method   | POST  |
| Path     | `/mvc/contact` |
| Content-Type | application/x-www-form-urlencoded |

**Body:** `name=Roni&email=roni@mail.com&message=hi`

**Test:**
```powershell
Invoke-WebRequest "http://localhost:8080/mvc/contact" `
  -Method Post `
  -ContentType "application/x-www-form-urlencoded" `
  -Body "name=Roni&email=roni@mail.com&message=hi" `
  -UseBasicParsing
```

---

## Authentication Endpoints

### Register a new user
```powershell
$body = @{
  username = "testuser"
  email    = "test@mail.com"
  password = "SecurePass123!"
} | ConvertTo-Json

Invoke-RestMethod "http://localhost:8080/auth/register" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

### Login (get JWT token)
```powershell
$body = @{
  username = "testuser"
  password = "SecurePass123!"
} | ConvertTo-Json

Invoke-RestMethod "http://localhost:8080/auth/login" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

---

## Status Codes Used

| Code | Meaning |
|------|---------|
| 200 OK | Valid request, normal response |
| 400 Bad Request | Validation failed / bad input |
| 401 Unauthorized | Missing or invalid JWT token |
| 403 Forbidden | Access denied (insufficient permissions) |
| 404 Not Found | Unknown route (no handler) |
| 415 Unsupported Media Type | Wrong Content-Type |
| 500 Internal Server Error | Unexpected server error |

---

## Project Structure

```
src/main/java/.../
├── controller/      # REST + MVC controllers (endpoints)
├── dto_/            # DTO classes for JSON/form input
├── entity/          # JPA entities (User, Note, Token)
├── repo/            # Spring Data JPA repositories
├── service/         # Business logic services
├── security/        # JWT filter, authentication
├── validation/      # Custom validation annotations
├── error/           # Global exception handler
└── config/          # Security & app configuration

src/main/resources/
├── application.properties   # App configuration
├── db/migration/            # Flyway database migrations
└── static/                  # Static files (index.html)
```

---

## Notes
- `.env` and `*.db` files are ignored by Git (not committed)
- Database migrations are handled automatically by **Flyway**
- JWT secret should be changed in production (`application.properties`)
- This lab includes security features: JWT authentication, rate limiting, security headers

---

## Troubleshooting

### Build errors on first run
If you get compilation errors, try cleaning the project first:
```powershell
.\mvnw.cmd clean
```

### Port already in use
If port 8080 is busy, stop the other process or change the port in `application.properties`:
```properties
server.port=8081
```

---

## Current Status
- ✅ Spring Boot application runs successfully
- ✅ SQLite database with Flyway migrations
- ✅ JWT authentication implemented
- ✅ Endpoints tested locally using browser + PowerShell
- ✅ Repository pushed to GitHub
