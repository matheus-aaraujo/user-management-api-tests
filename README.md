# user-management-api-tests

API tests for ServeRest's `GET /usuarios` endpoint, built with Java, REST Assured and JUnit.

## Requirements

- JDK 27 with `JAVA_HOME` configured.
- Internet access to download dependencies and reach https://serverest.dev.

Maven is provided by the included Wrapper; no global installation is required.

## Run tests

From the project root:

**Bash (Git Bash, Linux or macOS):**

```bash
bash ./mvnw clean test
```

**Windows PowerShell:**

```powershell
.\mvnw.cmd clean test
```

Reports are generated in `target/surefire-reports/`.

To use another API address, append `"-Dbase.url=http://localhost:3000"` to the command.

## What is validated

- User listing and details, including success response schemas.
- Filters by ID, name, email, password and administrator status.
- Combined filters, conflicting filters and empty results.
- Case-sensitive and partial ID searches returning no matches.
- Invalid or empty administrator and email filters.
- Unsupported parameters and repeated administrator values.
- HTTP status codes, JSON content type, validation error schemas and messages.

The suite creates two temporary users once and deletes them after execution.

GitHub Actions runs the tests on pull requests targeting `main` and pushes to `main`.
