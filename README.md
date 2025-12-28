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

---

## Requirements
- Java *JDK 21+*
- Git
- Maven *or* Maven Wrapper (included: mvnw, mvnw.cmd)

---

## How to Run

### Windows (PowerShell)
Open PowerShell inside the project root (where pom.xml exists) and run:

```powershell
.\mvnw.cmd spring-boot:run

macOS / Linux
bash
•/mvnw spring-boot:run
When the server starts, it runs on:
* http://localhost:8080
API Endpoints (Documentation)
1) Hello endpoint (basic GET)

Method: GET
Path: /hello
Returns: 200 OK (text/HTML)

Test:
	•	Browser: http://localhost:8080/hello
	•	PowerShell:
Invoke-WebRequest http://localhost:8080/hello -UseBasicParsing

2) Read request headers

Method: GET
Path: /api/headers
Headers (example): X-Student, X-Test
Returns: 200 OK (JSON)

Test:
Invoke-WebRequest "http://localhost:8080/api/headers" `
  -Headers @{ "X-Student"="Roni"; "X-Test"="123" } `
  -UseBasicParsing

3) Create user (JSON body + validation)

Method: POST
Path: /api/users
Request Body: JSON (DTO)
Example JSON:
{
  "username": "roni_123",
  "email": "roni@mail.com"
}

Success: 200 OK (JSON)
Validation Error: 400 Bad Request (structured error JSON)
Wrong Content-Type: 415 Unsupported Media Type

Test (Correct JSON):

$body = @{
  username = "roni_123"
  email    = "roni@mail.com"
} | ConvertTo-Json

Invoke-RestMethod "http://localhost:8080/api/users" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body

Test (Wrong Content-Type → 415):
Invoke-WebRequest "http://localhost:8080/api/users" `
  -Method Post `
  -ContentType "text/plain" `
  -Body "hi" `
  -UseBasicParsing

4) MVC form submit (form data parsing)

Method: POST
Path: /mvc/contact
Content-Type: application/x-www-form-urlencoded
Body (example): name=Roni&email=roni@mail.com&message=hi

Success: 200 OK (JSON response or MVC view depending on controller)

Test:Invoke-WebRequest "http://localhost:8080/mvc/contact" `
  -Method Post `
  -ContentType "application/x-www-form-urlencoded" `
  -Body "name=Roni&email=roni@mail.com&message=hi" `
  -UseBasicParsing

Status Codes Used

This project intentionally demonstrates correct HTTP status codes:
	•	200 OK → valid request, normal response
	•	400 Bad Request → validation failed / bad input
	•	404 Not Found → unknown route (no handler)
	•	415 Unsupported Media Type → wrong Content-Type (e.g., text/plain instead of JSON)
	•	500 Internal Server Error → unexpected server error (handled by global exception handler)

Project Structure

Main folders (important for the lab):
	•	src/main/java/.../controller
REST + MVC controllers (endpoints)
	•	src/main/java/.../dto
DTO classes for JSON/form input (request models)
	•	src/main/java/.../validation
Custom validation annotation + validator (e.g., username rules)
	•	src/main/java/.../error
Global exception handler (returns structured error responses)
	•	src/main/java/.../config
Security configuration (Spring Security)
	•	src/main/resources
Application config (application.properties) and resources
	•	pom.xml
Maven dependencies and build configuration

⸻

Notes
	•	.env and *.db files are ignored by Git (not committed).
	•	This lab will be extended with additional endpoints and security topics.

⸻

Current Status
	•	Spring Boot application runs successfully
	•	Endpoints tested locally using browser + PowerShell
	•	Repository pushed to GitHub
