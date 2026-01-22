# Lab 10-12: HTTP Server with Authentication

Spring Boot REST API with JWT authentication, role-based access control, and Notes CRUD.

## Tech Stack

- Java 21
- Spring Boot 4.0.0
- Spring Security + JWT
- SQLite + Flyway
- BCrypt password hashing

## Running the Server

```bash
./mvnw spring-boot:run
```

Server starts at `http://localhost:8080`

---

## API Endpoints

### Public

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /hello | Health check |
| POST | /auth/register | Register new user |
| POST | /auth/login | Login and get JWT |
| POST | /auth/logout | Logout |

### Protected (Require JWT)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /user/me | Get current user info |
| GET | /notes | List my notes |
| POST | /notes | Create a note |
| GET | /notes/{id} | Get single note |
| PUT | /notes/{id} | Update note |
| DELETE | /notes/{id} | Delete note |
| GET | /notes/count | Count my notes |
| GET | /notes/search?q=keyword | Search notes |

### Admin Only

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /admin/ping | Admin health check |
| GET | /admin/stats | System statistics |
| GET | /admin/users | List all users |

---

## Testing Commands

### 1. Check Server

```bash
curl http://localhost:8080/hello
```

### 2. Register

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"MySecure@Pass99"}'
```

Password requirements:
- 8+ characters
- Uppercase letter
- Lowercase letter
- Number
- Special character (!@#$%^&*)
- Not a common password

### 3. Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"MySecure@Pass99"}'
```

Copy the token from response.

### 4. Set Token

```bash
export TOKEN="your_token_here"
```

### 5. Get Profile

```bash
curl http://localhost:8080/user/me \
  -H "Authorization: Bearer $TOKEN"
```

### 6. Create Note

```bash
curl -X POST http://localhost:8080/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"My Note","content":"Some content"}'
```

### 7. List Notes

```bash
curl http://localhost:8080/notes \
  -H "Authorization: Bearer $TOKEN"
```

### 8. Get Single Note

```bash
curl http://localhost:8080/notes/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 9. Update Note

```bash
curl -X PUT http://localhost:8080/notes/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated Title","content":"New content"}'
```

### 10. Delete Note

```bash
curl -X DELETE http://localhost:8080/notes/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 11. Search Notes

```bash
curl "http://localhost:8080/notes/search?q=My" \
  -H "Authorization: Bearer $TOKEN"
```

### 12. Count Notes

```bash
curl http://localhost:8080/notes/count \
  -H "Authorization: Bearer $TOKEN"
```

### 13. Admin Endpoints (Returns 403 for regular users)

```bash
curl http://localhost:8080/admin/ping \
  -H "Authorization: Bearer $TOKEN"

curl http://localhost:8080/admin/stats \
  -H "Authorization: Bearer $TOKEN"
```

### 14. Logout

```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

---

## Error Responses

| Status | Meaning |
|--------|---------|
| 400 | Validation error |
| 401 | Not authenticated |
| 403 | Forbidden (wrong role) |
| 404 | Not found |
| 415 | Wrong content type |
| 500 | Server error |

---

## Testing Validation Errors

### Weak Password

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"weak"}'
```

### Invalid Email

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"notanemail","password":"MySecure@Pass99"}'
```

### Short Note Title

```bash
curl -X POST http://localhost:8080/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"ab","content":"test"}'
```

---

## Testing Access Control

Users can only see their own notes. To test:

1. Register user1, login, create a note
2. Register user2, login with user2's token
3. Try to access user1's note with user2's token - should return 404

---

## Project Structure

```
src/main/java/com/example/lab10/
├── config/          # Security config
├── controller/      # REST endpoints
├── dto_/            # Request/response objects
├── entity/          # JPA entities
├── error/           # Exception handlers
├── repo/            # Database repositories
├── security/        # JWT filter and utils
├── service/         # Business logic
└── validation/      # Custom validators
```

---

## Database

SQLite database stored in `database.db`. Migrations in `src/main/resources/db/migration/`.

---

## Running Tests

```bash
./mvnw test
```
