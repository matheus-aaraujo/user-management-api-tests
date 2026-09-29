package io.github.matheusamaral.users;

import io.github.matheusamaral.support.UserFixture;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.github.matheusamaral.support.ApiRequest.givenApi;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class CreateUserTest {

    private final UserFixture users = new UserFixture();
    String name = UUID.randomUUID().toString();

    @Test
    @DisplayName("Register a new user successfully")
    void shouldRegisterNewUser() {
        String unique = UUID.randomUUID().toString();
        String email = "create-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Create user test %s",
                  "email": "%s",
                  "password": "Test-%s",
                  "administrador": "false"
                }
                """.formatted(unique, email, unique);

        String userId = givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/create-user-success.schema.json"))
            .body("message", equalTo("Cadastro realizado com sucesso"))
            .body("_id", notNullValue())
            .extract().path("_id");

        users.deleteUser(userId);
    }

    @Test
    @DisplayName("Register a new user with administrator privileges successfully")
    void shouldRegisterNewUserAsAdministrator() {
        String unique = UUID.randomUUID().toString();
        String email = "create-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Create user test %s",
                  "email": "%s",
                  "password": "Test-%s",
                  "administrador": "true"
                }
                """.formatted(unique, email, unique);

        String userId = givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/create-user-success.schema.json"))
            .body("message", equalTo("Cadastro realizado com sucesso"))
            .body("_id", notNullValue())
            .extract().path("_id");

        users.deleteUser(userId);
    }

    @Test
    @DisplayName("Register users with the same name and different email addresses")
    void shouldRegisterUsersWithSameNameAndDifferentEmails() {
        String unique = UUID.randomUUID().toString();
        String email1 = "create-" + unique + "-1@example.com";
        String email2 = "create-" + unique + "-2@example.com";
        String requestBody1 = """
                {
                  "nome": "Create user test %s",
                  "email": "%s",
                  "password": "Test-%s",
                  "administrador": "false"
                }
                """.formatted(unique, email1, unique);
        String requestBody2 = """
                {
                  "nome": "Create user test %s",
                  "email": "%s",
                  "password": "Test-%s",
                  "administrador": "false"
                }
                """.formatted(unique, email2, unique);

        String userId1 = givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody1)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(201)
            .extract().path("_id");

        String userId2 = givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody2)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/create-user-success.schema.json"))
            .body("message", equalTo("Cadastro realizado com sucesso"))
            .body("_id", notNullValue())
            .extract().path("_id");

        users.deleteUser(userId1);
        users.deleteUser(userId2);
    }

    @Test
    @DisplayName("Reject a user without a name")
    void shouldRejectUserWithoutName() {
        String unique = UUID.randomUUID().toString();
        String email = "create-" + unique + "@example.com";
        String requestBody = """
                {
                  "email": "%s",
                  "password": "Test-%s",
                  "administrador": "false"
                }
                """.formatted(email, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("nome", equalTo("nome é obrigatório"));
    }

    @Test 
    @DisplayName("Reject a user without an email")
    void shouldRejectUserWithoutEmail() {
        String unique = UUID.randomUUID().toString();
        String requestBody = """
                {
                  "nome": "Reject user test %s",
                  "password": "Test-%s",
                  "administrador": "false"
                }
                """.formatted(unique, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("email", equalTo("email é obrigatório"));
    }

    @Test
    @DisplayName("Reject a user without a password")
    void shouldRejectUserWithoutPassword() {
        String unique = UUID.randomUUID().toString();
        String email = "create-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Reject user test %s",
                  "email": "%s",
                  "administrador": "false"
                }
                """.formatted(unique, email);

        givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("password", equalTo("password é obrigatório"));
    }

    @Test 
    @DisplayName("Reject a user without an administrator field")
    void shouldRejectUserWithoutAdministratorField() {
        String unique = UUID.randomUUID().toString();
        String email = "create-" + unique + "@example.com";
        String requestBody = """  
                {
                  "nome": "Reject user test %s",
                  "email": "%s",
                  "password": "Test-%s"
                }
                """.formatted(unique, email, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("administrador", equalTo("administrador é obrigatório"));
    }

    @Test
    @DisplayName("Reject a user with an invalid email format")
    void shouldRejectUserWithInvalidEmailFormat() {
        String unique = UUID.randomUUID().toString();
        String email = "invalid-email-format";
        String requestBody = """
                {
                  "nome": "Reject user test %s",
                  "email": "%s",
                  "password": "Test-%s",
                  "administrador": "false"
                }
                """.formatted(unique, email, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("email", equalTo("email deve ser um email válido"));
    }

    @Test
    @DisplayName("Reject an already registered email address")
    void shouldRejectAlreadyRegisteredEmail() {
        String unique = UUID.randomUUID().toString();
        String email = "create-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Create user test %s",
                  "email": "%s",
                  "password": "Test-%s",
                  "administrador": "false"
                }
                """.formatted(unique, email, unique);

        String userId = givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(201)
            .extract().path("_id");

        givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("message", equalTo("Este email já está sendo usado"));

        users.deleteUser(userId);
    }

    @Test
    @Disabled("Skipped: Unmapped scenario in the current API version.")
    @DisplayName("Reject a user with a password shorter than 6 characters")
    void shouldRejectUserWithShortPassword() {
        String unique = UUID.randomUUID().toString();
        String email = "create-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Reject user test %s",
                  "email": "%s",
                  "password": "Test-%s",
                  "administrador": "false"
                }
                """.formatted(unique, email, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("password", equalTo("password deve ter pelo menos 6 caracteres"));
    }

    @Test
    @Disabled("Skipped: Unmapped scenario in the current API version.")
    @DisplayName("Reject a user with a password longer than 12 characters")
    void shouldRejectUserWithLongPassword() {
        String unique = UUID.randomUUID().toString();
        String email = "create-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Reject user test %s",
                  "email": "%s",
                  "password": "Test-%s-1234567890",
                  "administrador": "false"
                }
                """.formatted(unique, email, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("password", equalTo("password deve ter no máximo 12 caracteres"));
    }

    @Test
    @DisplayName("Reject a user with an invalid administrator field")
    void shouldRejectUserWithInvalidAdministratorField() {
        String unique = UUID.randomUUID().toString();
        String email = "create-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Reject user test %s",
                  "email": "%s",
                  "password": "Test-%s",
                  "administrador": "invalid"
                }
                """.formatted(unique, email, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("administrador", equalTo("administrador deve ser 'true' ou 'false'"));
    }

    @Test 
    @DisplayName("Reject a user with an empty name")
    void shouldRejectUserWithEmptyName() {
        String unique = UUID.randomUUID().toString();
        String email = "create-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "",
                  "email": "%s",
                  "password": "Test-%s",
                  "administrador": "false"
                }
                """.formatted(email, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("nome", equalTo("nome não pode ficar em branco"));
    }

    @Test
    @DisplayName("Reject a user with an empty administrator field")
    void shouldRejectUserWithEmptyAdministratorField() {
        String unique = UUID.randomUUID().toString();
        String email = "create-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Reject user test %s",
                  "email": "%s",
                  "password": "Test-%s",
                  "administrador": ""
                }
                """.formatted(unique, email, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("administrador", equalTo("administrador deve ser 'true' ou 'false'"));
    }

    @Test
    @DisplayName("Reject a user with an empty email field")
    void shouldRejectUserWithEmptyEmailField() {
        String unique = UUID.randomUUID().toString();
        String requestBody = """
                {
                  "nome": "Reject user test %s",
                  "email": "",
                  "password": "Test-%s",
                  "administrador": "false"
                }
                """.formatted(unique, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("email", equalTo("email não pode ficar em branco"));
    }

    @Test 
    @DisplayName("Reject a user with an empty password field")
    void shouldRejectUserWithEmptyPasswordField() {
        String unique = UUID.randomUUID().toString();
        String email = "create-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Reject user test %s",
                  "email": "%s",
                  "password": "",
                  "administrador": "false"
                }
                """.formatted(unique, email);

        givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("password", equalTo("password não pode ficar em branco"));
    }

    @Test
    @DisplayName("Reject unsupported fields in the registration payload")
    void shouldRejectUserWithUnsupportedFields() {
        String unique = UUID.randomUUID().toString();
        String email = "create-" + unique + "@example.com";
        String requestBody = """
                {
                  "nome": "Reject user test %s",
                  "email": "%s",
                  "password": "Test-%s",
                  "administrador": "false",
                  "unsupported_field": "value"
                }
                """.formatted(unique, email, unique);

        givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("unsupported_field", equalTo("unsupported_field não é permitido"));
    }
}