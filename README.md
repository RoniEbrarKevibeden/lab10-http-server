\# Lab 10 – HTTP Server (Spring Boot)



This project is the initial setup for \*\*Lab 10\*\*.  

It starts a \*\*Spring Boot HTTP application server\*\* and provides a simple endpoint to verify that the server is running correctly.



---



\## Requirements



\- Java \*\*JDK 21+\*\* (recommended)

\- Maven (or the included \*\*Maven Wrapper\*\* `mvnw`)

\- Git



---



\## How to Run



\### Windows (PowerShell)



```powershell

\# (Optional) set JAVA\_HOME if needed

$env:JAVA\_HOME="C:\\Program Files\\Java\\jdk-23"

$env:Path="$env:JAVA\_HOME\\bin;$env:Path"



\# run the application

.\\mvnw.cmd spring-boot:run



macOS / Linux

./mvnw spring-boot:run



Application URL

After the application starts, it will be available at:

http://localhost:8080



Test Endpoint

Open the following URL in your browser or via curl:

http://localhost:8080/hello



Expected Response

Hello from Lab 10 HTTP Server!



Project Structure

Important folders and files:

src/main/java/.../controller

-REST controllers (HTTP endpoints)

src/main/resources

-Application configuration and resources

pom.xml

-Maven dependencies and build configuration



Notes

-.env and \*.db files are ignored by Git and are not committed.

-This project will be extended in later labs with:

&nbsp; Additional HTTP verbs (GET, POST, PUT, DELETE)

&nbsp; Spring Security configuration



Status

Spring Boot application runs successfully

/hello endpoint works correctly

Repository pushed to GitHub

---

