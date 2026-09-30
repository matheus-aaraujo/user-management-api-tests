package io.github.matheusamaral.users;

import io.github.matheusamaral.support.UserFixture;
import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.UUID;

import static io.github.matheusamaral.support.ApiRequest.givenApi;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class UpdateUserTest {

    private final UserFixture users = new UserFixture();

    @BeforeAll
    void setUp() {
        users.create();
    }

    @AfterAll
    void tearDown() {
        users.cleanup();
    }

    @Test
    @DisplayName("Update an existing user successfully")
    void shouldUpdateExistingUser() {
        String unique = UUID.randomUUID().toString();
        String email = "updated-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Updated user %s",
                  "email": "%s",
                  "password": "Updated-%s",
                  "administrador": "true"
                }
                """.formatted(unique, email, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .pathParam("id", users.regularUser().id())
            .body(requestBody)
        .when()
            .put("/usuarios/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/update-user-success.schema.json"))
            .body("message", equalTo("Registro alterado com sucesso"));
    }

    @Test
    @DisplayName("Create a user when updating a non-existent identifier")
    void shouldCreateUserWhenUpdatingNonExistentId() {
        String unique = UUID.randomUUID().toString();
        String email = "new-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "New user %s",
                  "email": "%s",
                  "password": "New-%s",
                  "administrador": "false"
                }
                """.formatted(unique, email, unique);

        String missingUserId = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 16);

        givenApi()
            .contentType(ContentType.JSON)
            .pathParam("id", missingUserId)
            .body(requestBody)
        .when()
            .put("/usuarios/{id}")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/create-user-success.schema.json"))
            .body("message", equalTo("Cadastro realizado com sucesso"));
    }

    @Test
    @DisplayName("Reject an update using an email address that belongs to another user")
    void shouldRejectUpdateUsingExistingEmail() {
        String unique = UUID.randomUUID().toString();
        String requestBody = """
                {
                  "nome": "Updated user %s",
                  "email": "%s",
                  "password": "Updated-%s",
                  "administrador": "true"
                }
                """.formatted(unique, users.administratorUser().email(), unique);

        givenApi()
            .contentType(ContentType.JSON)
            .pathParam("id", users.regularUser().id())
            .body(requestBody)
        .when()
            .put("/usuarios/{id}")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("message", equalTo("Este email já está sendo usado"));
    }

    @Test
    @DisplayName("Reject an update without a name")
    void shouldRejectUpdateWithoutName() {
        String unique = UUID.randomUUID().toString();
        String email = "updated-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "",
                  "email": "%s",
                  "password": "Updated-%s",
                  "administrador": "true"
                }
                """.formatted(email, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .pathParam("id", users.regularUser().id())
            .body(requestBody)
        .when()
            .put("/usuarios/{id}")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("nome", equalTo("nome não pode ficar em branco"));
    }

    @Test 
    @DisplayName("Reject an update without an email address")
    void shouldRejectUpdateWithoutEmail() {
        String unique = UUID.randomUUID().toString();
        String requestBody = """
                {
                  "nome": "Updated user %s",
                  "email": "",
                  "password": "Updated-%s",
                  "administrador": "true"
                }
                """.formatted(unique, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .pathParam("id", users.regularUser().id())
            .body(requestBody)
        .when()
            .put("/usuarios/{id}")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("email", equalTo("email não pode ficar em branco"));
    }

    @Test
    @DisplayName("Reject an update without a password")
    void shouldRejectUpdateWithoutPassword() {
        String unique = UUID.randomUUID().toString();
        String email = "updated-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Updated user %s",
                  "email": "%s",
                  "password": "",
                  "administrador": "true"
                }
                """.formatted(unique, email);

        givenApi()
            .contentType(ContentType.JSON)
            .pathParam("id", users.regularUser().id())
            .body(requestBody)
        .when()
            .put("/usuarios/{id}")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("password", equalTo("password não pode ficar em branco"));
    }

    @Test
    @DisplayName("Reject an update without an administrator flag")
    void shouldRejectUpdateWithoutAdministratorFlag() {
        String unique = UUID.randomUUID().toString();
        String email = "updated-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Updated user %s",
                  "email": "%s",
                  "password": "Updated-%s",
                  "administrador": ""
                }
                """.formatted(unique, email, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .pathParam("id", users.regularUser().id())
            .body(requestBody)
        .when()
            .put("/usuarios/{id}")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("administrador", equalTo("administrador deve ser 'true' ou 'false'"));
    }

    @Test 
    @DisplayName("Reject an update with an invalid email address")
    void shouldRejectUpdateWithInvalidEmail() {
        String unique = UUID.randomUUID().toString();
        String requestBody = """
                {
                  "nome": "Updated user %s",
                  "email": "invalid-email",
                  "password": "Updated-%s",
                  "administrador": "true"
                }
                """.formatted(unique, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .pathParam("id", users.regularUser().id())
            .body(requestBody)
        .when()
            .put("/usuarios/{id}")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("email", equalTo("email deve ser um email válido"));
    }

    @Test 
    @DisplayName ("Reject an update with an invalid administrator flag")
    void shouldRejectUpdateWithInvalidAdministratorFlag() {
        String unique = UUID.randomUUID().toString();
        String email = "updated-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Updated user %s",
                  "email": "%s",
                  "password": "Updated-%s",
                  "administrador": "invalid-flag"
                }
                """.formatted(unique, email, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .pathParam("id", users.regularUser().id())
            .body(requestBody)
        .when()
            .put("/usuarios/{id}")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("administrador", equalTo("administrador deve ser 'true' ou 'false'"));
    }

    @Test
    @DisplayName("Create a user when updating with a blank identifier")
    void shouldCreateUserWhenUpdatingWithBlankUserId() {
        String unique = UUID.randomUUID().toString();
        String email = "updated-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Updated user %s",
                  "email": "%s",
                  "password": "Updated-%s",
                  "administrador": "true"
                }
                """.formatted(unique, email, unique);

        String userId = givenApi()
            .contentType(ContentType.JSON)
            .pathParam("id", " ")
            .body(requestBody)
        .when()
            .put("/usuarios/{id}")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/create-user-success.schema.json"))
            .body("message", equalTo("Cadastro realizado com sucesso"))
            .extract().path("_id");

        users.deleteUser(userId);
    }

    @Test
    @Tag("known-bug")
    @Description("Expected behavior: PUT /usuarios/{id} should reject identifiers that do not have exactly 16 alphanumeric characters. Current API behavior creates a user instead.")
    @DisplayName("Reject an update when the user identifier format is invalid")
    void shouldRejectUpdateWhenUserIdFormatIsInvalid() {
        String unique = UUID.randomUUID().toString();
        String email = "updated-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Updated user %s",
                  "email": "%s",
                  "password": "Updated-%s",
                  "administrador": "true"
                }
                """.formatted(unique, email, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .pathParam("id", "invalid-id-value")
            .body(requestBody)
        .when()
            .put("/usuarios/{id}")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("id", equalTo("id deve ter exatamente 16 caracteres alfanuméricos"));
    }

    @Test 
    @DisplayName("Reject unsupported fields in the update payload")
    void shouldRejectUnsupportedFieldsInUpdatePayload() {
        String unique = UUID.randomUUID().toString();
        String email = "updated-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Updated user %s",
                  "email": "%s",
                  "password": "Updated-%s",
                  "administrador": "true",
                  "unsupportedField": "value"
                }
                """.formatted(unique, email, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .pathParam("id", users.regularUser().id())
            .body(requestBody)
        .when()
            .put("/usuarios/{id}")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("unsupportedField", equalTo("unsupportedField não é permitido"));
    }
}
