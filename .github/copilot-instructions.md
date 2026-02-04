# Copilot Instructions for Lab 10 HTTP Server

## Project Overview
Lab 10 is a Spring Boot 4.0.0 (Java 21) HTTP server demonstrating secure request processing, validation, and proper HTTP response handling. It exposes REST and form-based endpoints with comprehensive error handling using a global exception handler.

## Architecture & Key Patterns

### Request Processing Pipeline
1. **Controllers** → Route requests to appropriate handlers
2. **DTO Validation** → Apply constraints via Jakarta validation annotations
3. **GlobalExceptionHandler** → Centralize error responses (400, 404, 415, 500)

Controllers are organized by consumption type:
- `ApiController` (`/api/*`): JSON-based REST endpoints with `@RequestBody` and `@RequestParam`
- `MvcController` (`/mvc/*`): Form-based endpoints with `@ModelAttribute` and `application/x-www-form-urlencoded`
- `HelloController`: Simple `@GetMapping` with no body parsing

### Validation Patterns
- **Built-in constraints**: `@NotBlank`, `@Email`, `@Size` (jakarta.validation.constraints)
- **Custom validators**: `@Username` annotation paired with `UsernameValidator` class
  - Custom validators implement `ConstraintValidator<CustomAnnotation, Type>`
  - Regex validation example: `^[a-zA-Z0-9_]{3,20}$` for username
  - Place validators in `validation/` package, annotations alongside
- **DTO fields use getter/setter** (not Lombok) for explicit control
- Apply `@Valid` on `@RequestBody` in controller methods; GlobalExceptionHandler catches violations

### Error Handling (`GlobalExceptionHandler`)
- `@RestControllerAdvice` catches exceptions globally
- Returns structured JSON: `{"error": "code", "status": XXX, ...}`
- Specific handlers by exception type:
  - `MethodArgumentNotValidException` → 400 with `fields` object mapping constraint violations
  - `HttpMediaTypeNotSupportedException` → 415
  - `NoHandlerFoundException` → 404
  - `Exception` (fallback) → 500

### MediaType Specifications
- JSON endpoints: `consumes = MediaType.APPLICATION_JSON_VALUE` on `@PostMapping` (required)
- Form endpoints: `consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE`
- Always specify explicit consumes; omitting it may accept unintended types

## Build & Run Commands

### Run Server (Windows PowerShell)
```powershell
.\mvnw.cmd spring-boot:run
```
Server starts on `http://localhost:8080`

### Compile & Package
```powershell
.\mvnw.cmd clean package
```

### Run Tests
```powershell
.\mvnw.cmd test
```

### Database Migrations
- Flyway migrations in `src/main/resources/db/migration/` (naming: `V1__description.sql`)
- SQLite database: `database.db` (created at startup)
- `spring.jpa.hibernate.ddl-auto=validate` (never auto-create schema)

## Configuration & Dependencies

### Key Dependencies
- **spring-boot-starter-web**: REST controller, request mapping
- **spring-boot-starter-security**: CSRF disabled, all endpoints permit-all (`SecurityConfig.java`)
- **spring-boot-starter-validation**: Jakarta validation (constraints, validators)
- **sqlite-jdbc + hibernate-community-dialects**: SQLite persistence
- **spring-boot-starter-data-jpa**: ORM with JPA repositories
- **spring-boot-starter-flyway**: Database migration management

### Application Properties
- Environment-based config: `.env[.properties]` imported via `spring.config.import`
- Database credentials: `DB_USERNAME`, `DB_PASSWORD` from env vars
- JPA logging: `spring.jpa.show-sql=true` for debugging queries
- Security: CSRF disabled, all requests `permitAll()` (no authentication required)

## Project Conventions

### Package Structure & Naming
- **dto_** (underscore suffix): Data transfer objects with validation annotations
- **validation**: Custom constraint annotations + validator implementations
- **error**: Global exception handlers
- **config**: Spring security, bean definitions
- **controller**: HTTP request handlers (split by consumption type)

### HTTP Status Code Mapping
- 200 OK: Valid request, successful processing (default)
- 400 Bad Request: Validation failures (constraint violations, missing fields)
- 404 Not Found: Route doesn't exist
- 415 Unsupported Media Type: `consumes` mismatch (e.g., form-urlencoded sent to JSON endpoint)
- 500 Internal Server Error: Unhandled exceptions

### Request/Response Patterns
**JSON Endpoint** (ApiController):
```java
@PostMapping(value = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
public Map<String, Object> createUser(@Valid @RequestBody UserCreateRequest body) {
    return Map.of("status", "created", "username", body.getUsername());
}
```

**Query Params** (ApiController):
```java
@GetMapping("/echo")
public Map<String, Object> echo(@RequestParam(defaultValue = "empty") String msg) {
    return Map.of("msg", msg);
}
```

**Headers** (ApiController):
```java
@GetMapping("/headers")
public Map<String, Object> headers(
    @RequestHeader(value = "User-Agent", required = false) String userAgent
) {
    return Map.of("userAgent", userAgent);
}
```

## Common Tasks

### Adding a New REST Endpoint
1. Create DTO in `dto_/` with validation constraints (`@NotBlank`, `@Email`, `@Valid`, custom validators)
2. Add handler method to `ApiController` with `@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)` or `@GetMapping`
3. Use `@Valid @RequestBody` for JSON bodies; GlobalExceptionHandler automatically catches validation errors
4. Return `Map<String, Object>` or domain model; Jackson serializes to JSON

### Adding a Custom Validator
1. Create annotation in `validation/`: `@Target(ElementType.FIELD) @Retention(RetentionPolicy.RUNTIME) @Constraint(validatedBy = MyValidator.class)`
2. Create validator class: `implements ConstraintValidator<MyAnnotation, String>`
3. Implement `isValid()` with validation logic (e.g., regex matching)
4. Apply annotation to DTO field with `@NotBlank` and custom annotation stacked

### Troubleshooting Validation Failures
- GlobalExceptionHandler returns 400 with `fields` map: `{"error": "validation_failed", "status": 400, "fields": {"username": "message"}}`
- Check DTO getter/setter names match field names
- Ensure `@Valid` is on `@RequestBody` parameter
- Remember: custom validators can return true for null (let @NotBlank handle nulls)

### Adding Custom Validation
1. Define annotation in `validation/` with `@Constraint(validatedBy = YourValidator.class)`
2. Implement `ConstraintValidator<YourAnnotation, Type>` in same package
3. Apply annotation to DTO field alongside other `jakarta.validation` constraints
4. Example: `@Username` uses regex `^[a-zA-Z0-9_]{3,20}$`

### Handling New Exception Type
1. Add `@ExceptionHandler(YourException.class)` method to `GlobalExceptionHandler`
2. Return `ResponseEntity.status(HttpStatus.XXX).body(Map.of(...))`
3. Include `error` key (error code), `status` key (HTTP status), and relevant details
