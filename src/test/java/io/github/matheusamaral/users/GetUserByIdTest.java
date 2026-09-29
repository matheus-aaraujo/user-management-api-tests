package io.github.matheusamaral.users;

import io.github.matheusamaral.support.UserFixture;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.UUID;

import static io.github.matheusamaral.support.ApiRequest.givenApi;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class GetUserByIdTest {

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
    @DisplayName("Retrieve a user profile by its identifier")
    void shouldRetrieveUserById() {
        givenApi()
            .pathParam("id", users.regularUser().id())
        .when()
            .get("/usuarios/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/user-details-success.schema.json"))
            .body("nome", equalTo(users.regularUser().name()))
            .body("email", equalTo(users.regularUser().email()))
            .body("password", equalTo(users.regularUser().password()))
            .body("administrador", equalTo("false"))
            .body("_id", equalTo(users.regularUser().id()));
    }

    @Test
    @DisplayName("Return not found when the user identifier does not exist")
    void shouldReturnNotFoundWhenUserIdDoesNotExist() {
        String missingUserId = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 16);

        givenApi()
            .pathParam("id", missingUserId)
        .when()
            .get("/usuarios/{id}")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("message", equalTo("Usuário não encontrado"));
    }

    @Test
    @DisplayName("Return not found when the user identifier is invalid")
    void shouldReturnNotFoundWhenUserIdIsInvalid() {
        givenApi()
            .pathParam("id", "invalid-id-value")
        .when()
            .get("/usuarios/{id}")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("id", equalTo("id deve ter exatamente 16 caracteres alfanuméricos"));
    }

    @Test
    @DisplayName("Reject a blank user identifier")
    void shouldRejectBlankUserId() {
        givenApi()
            .pathParam("id", " ")
        .when()
            .get("/usuarios/{id}")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("id", equalTo("id deve ter exatamente 16 caracteres alfanuméricos"));
    }

    @Test
    @DisplayName("Return not found when the user identifier letter case differs")
    void shouldReturnNotFoundWhenUserIdLetterCaseDiffers() {
        givenApi()
            .pathParam("id", users.regularUser().id().toUpperCase())
        .when()
            .get("/usuarios/{id}")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("message", equalTo("Usuário não encontrado"));
    }

    @Test 
    @DisplayName("Return not found when only part of the user identifier is provided")
    void shouldReturnNotFoundWhenOnlyPartOfUserIdIsProvided() {
        String partialId = users.regularUser().id().substring(0, 5);
        givenApi()
            .pathParam("id", partialId)
        .when()
            .get("/usuarios/{id}")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("id", equalTo("id deve ter exatamente 16 caracteres alfanuméricos"));
    }
}
