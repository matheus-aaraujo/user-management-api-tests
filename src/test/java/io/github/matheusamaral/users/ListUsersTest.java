package io.github.matheusamaral.users;

import io.github.matheusamaral.support.UserFixture;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.UUID;

import static io.github.matheusamaral.support.ApiRequest.givenApi;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class ListUsersTest {

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
    @DisplayName("Retrieve the complete list of users")
    void shouldListUsers() {
        givenApi()
        .when()
            .get("/usuarios")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/list-users-success.schema.json"));
            //.body("usuarios._id", hasItems(users.regularUser().id(), users.administratorUser().id()));
    }

    @Test
    @DisplayName("Return an empty list when no users match the search criteria")
    void shouldReturnNoMatches() {
        givenApi()
            .queryParam("nome", "Missing user " + UUID.randomUUID())
        .when()
            .get("/usuarios")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/list-users-success.schema.json"))
            .body("quantidade", equalTo(0))
            .body("usuarios", empty());
    }

    @Test
    @DisplayName("List users with administrator privileges")
    void shouldListAdministrators() {
        givenApi()
            .queryParam("administrador", "true")
        .when()
            .get("/usuarios")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/list-users-success.schema.json"))
            .body("usuarios._id", hasItem(users.administratorUser().id()))
            .body("usuarios._id", not(hasItem(users.regularUser().id())))
            .body("usuarios.administrador", everyItem(equalTo("true")));
    }

    @Test
    @DisplayName("List users without administrator privileges")
    void shouldListNonAdministrators() {
        givenApi()
            .queryParam("administrador", "false")
        .when()
            .get("/usuarios")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/list-users-success.schema.json"))
            .body("usuarios._id", hasItem(users.regularUser().id()))
            .body("usuarios._id", not(hasItem(users.administratorUser().id())))
            .body("usuarios.administrador", everyItem(equalTo("false")));
    }

    @Test
    @DisplayName("Find a user by their unique identifier")
    void shouldFindUserById() {
        givenApi()
            .queryParam("_id", users.regularUser().id())
        .when()
            .get("/usuarios")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/list-users-success.schema.json"))
            .body("quantidade", equalTo(1))
            .body("usuarios._id", contains(users.regularUser().id()));
    }

    @Test
    @DisplayName("Find users by name")
    void shouldFindUsersByName() {
        givenApi()
            .queryParam("nome", users.regularUser().name())
        .when()
            .get("/usuarios")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/list-users-success.schema.json"))
            .body("quantidade", equalTo(1))
            .body("usuarios._id", contains(users.regularUser().id()))
            .body("usuarios.nome", contains(users.regularUser().name()));
    }

    @Test
    @DisplayName("Find a user by email address")
    void shouldFindUserByEmail() {
        givenApi()
            .queryParam("email", users.regularUser().email())
        .when()
            .get("/usuarios")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/list-users-success.schema.json"))
            .body("quantidade", equalTo(1))
            .body("usuarios._id", contains(users.regularUser().id()))
            .body("usuarios.email", contains(users.regularUser().email()));
    }

    @Test
    @DisplayName("Find users matching the specified password")
    void shouldFindUsersByPassword() {
        givenApi()
            .queryParam("password", users.regularUser().password())
        .when()
            .get("/usuarios")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/list-users-success.schema.json"))
            .body("quantidade", equalTo(1))
            .body("usuarios._id", contains(users.regularUser().id()))
            .body("usuarios.password", contains(users.regularUser().password()));
    }

    @Test
    @DisplayName("Find users matching all search criteria")
    void shouldCombineFilters() {
        givenApi()
            .queryParam("_id", users.regularUser().id())
            .queryParam("nome", users.regularUser().name())
            .queryParam("email", users.regularUser().email())
            .queryParam("password", users.regularUser().password())
            .queryParam("administrador", "false")
        .when()
            .get("/usuarios")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/list-users-success.schema.json"))
            .body("quantidade", equalTo(1))
            .body("usuarios._id", contains(users.regularUser().id()));
    }

    @Test
    @DisplayName("Return no users when search criteria conflict")
    void shouldReturnNoUsersForConflictingFilters() {
        givenApi()
            .queryParam("_id", users.regularUser().id())
            .queryParam("email", users.administratorUser().email())
        .when()
            .get("/usuarios")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/list-users-success.schema.json"))
            .body("quantidade", equalTo(0))
            .body("usuarios", empty());
    }

    @Test
    @DisplayName("Include all required user details in the results")
    void shouldIncludeUserDetails() {
        givenApi()
            .queryParam("_id", users.regularUser().id())
        .when()
            .get("/usuarios")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/list-users-success.schema.json"))
            .body("quantidade", equalTo(1))
            .body("usuarios._id", contains(users.regularUser().id()))
            .body("usuarios.nome", contains(users.regularUser().name()))
            .body("usuarios.email", contains(users.regularUser().email()))
            .body("usuarios.password", contains(users.regularUser().password()))
            .body("usuarios.administrador", contains("false"));
    }

    @ParameterizedTest(name = "{displayName} [{0}]")
    @ValueSource(strings = { "invalid", "TRUE", "1" })
    @DisplayName("Reject an unsupported administrator filter value")
    void shouldRejectUnsupportedAdministrator(String administrator) {
        givenApi()
            .queryParam("administrador", administrator)
        .when()
            .get("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("administrador", equalTo("administrador deve ser 'true' ou 'false'"));
    }

    @Test
    @DisplayName("Reject an empty administrator filter")
    void shouldRejectEmptyAdministrator() {
        givenApi()
            .queryParam("administrador", "")
        .when()
            .get("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("administrador", equalTo("administrador deve ser 'true' ou 'false'"));
    }

    @Test
    @DisplayName("Reject an invalid email address filter")
    void shouldRejectInvalidEmail() {
        givenApi()
            .queryParam("email", "not-an-email")
        .when()
            .get("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("email", equalTo("email deve ser um email válido"));
    }

    @Test
    @DisplayName("Reject an empty email address filter")
    void shouldRejectEmptyEmail() {
        givenApi()
            .queryParam("email", "")
        .when()
            .get("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("email", equalTo("email deve ser uma string"));
    }

    @Test
    @DisplayName("Reject unsupported search parameters")
    void shouldRejectUnsupportedSearchParameter() {
        givenApi()
            .queryParam("unexpected", "value")
        .when()
            .get("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("unexpected", equalTo("unexpected não é permitido"));
    }

    @Test
    @DisplayName("Reject multiple values for the administrator filter")
    void shouldRejectMultipleAdministratorValues() {
        givenApi()
            .queryParam("administrador", "true")
            .queryParam("administrador", "false")
        .when()
            .get("/usuarios")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("administrador", equalTo("administrador deve ser 'true' ou 'false'"));
    }

    @Test
    @DisplayName("Return no users when the identifier letter case differs")
    void shouldReturnNoUsersWhenIdCaseDiffers() {
        String changedId = users.regularUser().id().toUpperCase(java.util.Locale.ROOT);

        givenApi()
            .queryParam("_id", changedId)
        .when()
            .get("/usuarios")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/list-users-success.schema.json"))
            .body("quantidade", equalTo(0))
            .body("usuarios", empty());
    }

    @Test
    @DisplayName("Return no users when only part of the identifier is provided")
    void shouldReturnNoUsersWhenIdIsPartial() {
        String partialId = users.regularUser().id().substring(0, 8);

        givenApi()
            .queryParam("_id", partialId)
        .when()
            .get("/usuarios")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/list-users-success.schema.json"))
            .body("quantidade", equalTo(0))
            .body("usuarios", empty());
    }
}
