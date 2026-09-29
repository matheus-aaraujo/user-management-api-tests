# user-management-api-tests

Automated API tests for a user management application, built with REST Assured and covering user creation, listing, retrieval, updates, and deletion.

## Requirements

- JDK 27, with JAVA_HOME pointing to the JDK directory.
- Internet access for the first build to download Maven and dependencies.

## Run

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Linux/macOS:

```sh
./mvnw test
```

The Maven Wrapper pins the Maven version; a global Maven installation is not required.

This initial scaffold contains no API tests yet. A successful build validates project setup, not API behavior. The API base URL and request/response contracts will be configured when tests are added.

## Structure

- `pom.xml`: project metadata, dependencies and build plugins.
- `src/test/java/`: test classes and test support code.
- `src/test/resources/`: test resources.
- `.mvn/wrapper/`, `mvnw`, `mvnw.cmd`: Maven Wrapper.
- `target/`: generated build output (ignored by Git).

The `.gitkeep` files preserve empty directories and can be removed once those directories contain files. Local IDE settings are ignored because they may contain machine-specific paths.

## Branches and commits

Use short-lived branches named `type/short-description`, in lowercase English with hyphens. Add an issue number when useful, for example `test/12-list-users`.

Examples:

- `chore/initialize-project`
- `test/list-users`
- `test/create-user`
- `fix/user-id-assertion`
- `docs/setup-instructions`

Keep each commit focused on one coherent change.
