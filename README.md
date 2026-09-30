# user-management-api-tests

API tests for ServeRest's user management endpoints, built with Java, REST Assured, JUnit and Allure.

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

The full suite includes one known failing scenario tagged as `known-bug`. To run only the stable scenarios, exclude that tag:

```powershell
.\mvnw.cmd clean test -DexcludedGroups=known-bug
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

## Authentication

ServeRest uses JWT authentication for protected resources, such as products and carts.

The user endpoints covered by this project, such as `GET /usuarios`, `POST /usuarios`, `PUT /usuarios/{id}` and `DELETE /usuarios/{id}`, currently accept requests without an authentication token. During exploratory validation, `PUT /usuarios/{id}` and `DELETE /usuarios/{id}` also accepted an `Authorization` header, but did not require it.

Because of that, authentication is used only in test setup for protected endpoints needed by user scenarios, such as creating products and carts before validating user deletion rules.

## What is validated

### GET /usuarios

- Complete user listing and success response schema.
- Empty results when no users match the search criteria.
- Filters by ID, name, email, password and administrator status.
- Combined filters, conflicting filters and required user details in the response.
- Case-sensitive and partial ID searches returning no matches.
- Invalid, empty and repeated administrator filters.
- Invalid and empty email filters.
- Unsupported query parameters.

### POST /usuarios

- Successful registration for regular and administrator users.
- Registration of users with the same name and different email addresses.
- Required fields: name, email, password and administrator.
- Empty values for name, email, password and administrator.
- Invalid email format and invalid administrator value.
- Duplicate email rejection.
- Unsupported fields in the registration payload.
- Password length scenarios are mapped but disabled because the current API version does not enforce those rules.

### GET /usuarios/{id}

- Retrieval of an existing user by ID.
- Not found response for a valid 16-character alphanumeric ID that does not exist.
- Validation error for invalid, blank and partial IDs.
- Not found response when the ID letter case differs.

### PUT /usuarios/{id}

- Successful update of an existing user.
- User creation when updating a valid non-existent ID.
- User creation when updating with a blank identifier, matching the current API behavior.
- Rejection when using an email address that belongs to another user.
- Required/blank field validations for name, email, password and administrator.
- Invalid email format and invalid administrator value.
- Unsupported fields in the update payload.

### DELETE /usuarios/{id}

- Successful removal of an existing user.
- Prevention of removing a user with a registered cart.
- Success message when removing a non-existent, invalid, blank or whitespace identifier.

## Known Bug

`PUT /usuarios/{id}` currently creates a user when the identifier has an invalid format, such as `invalid-id-value`.

The expected behavior documented by the test is to reject identifiers that do not have exactly 16 alphanumeric characters. The scenario is intentionally kept as a failing test and tagged as `known-bug` so the Allure report shows the API gap clearly.

## Test Data

The suite creates temporary users during execution and removes them afterward. Some scenarios also create products and carts as setup data, using JWT authentication only where the API requires it.

GitHub Actions runs the tests on pull requests targeting `main` and pushes to `main`.