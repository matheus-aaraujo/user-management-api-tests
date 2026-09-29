# user-management-api-tests

API tests for ServeRest's user management endpoints, built with Java, REST Assured and JUnit.

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

## Allure report

To generate the Allure report locally, run:

```powershell
.\mvnw.cmd clean test allure:report
```

The HTML report is generated in `target/site/allure-maven-plugin/`.

Do not open `index.html` directly with `file://`, because the report can stay stuck on `Loading...`. Serve the folder through a local HTTP server instead:

```powershell
cd target/site/allure-maven-plugin
python -m http.server 8080
```

Then open `http://localhost:8080` in the browser.

In GitHub Actions, pull requests upload the Allure report as an artifact. After downloading and extracting the zip, open it using the same local server approach. Pushes to `main` publish the report to GitHub Pages, making it available through a clickable URL in the action run.

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