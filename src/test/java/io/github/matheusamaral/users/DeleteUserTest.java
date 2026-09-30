package io.github.matheusamaral.users;

import io.github.matheusamaral.support.UserFixture;
import io.github.matheusamaral.support.UserFixture.Product;
import io.github.matheusamaral.support.UserFixture.User;
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
class DeleteUserTest {

    private final UserFixture users = new UserFixture();
    private User userToRemove;
    private User userWithCart;

    @BeforeAll
    void setUp() {
        String unique = UUID.randomUUID().toString();
        User administrator = users.createUser("Delete admin " + unique,
                "delete-admin-" + unique + "@example.com", "Admin-" + unique, "true");
        userToRemove = users.createUser("Delete user test " + unique,
                "delete-" + unique + "@example.com", "Test-" + unique, "false");
        userWithCart = users.createUser("Delete user with cart " + unique,
                "delete-cart-" + unique + "@example.com", "Cart-" + unique, "false");
        Product product = users.createProduct(administrator);
        users.createCart(userWithCart, product);
    }

    @AfterAll
    void tearDown() {
        users.cleanup();
    }

    @Test
    @DisplayName("Remove an existing user successfully")
    void shouldRemoveExistingUser() {
        givenApi()
            .pathParam("id", userToRemove.id())
        .when()
            .delete("/usuarios/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/delete-user-success.schema.json"))
            .body("message", equalTo("Registro excluído com sucesso"));
    }

    @Test
    @DisplayName("Prevent removing a user with a registered cart")
    void shouldPreventRemovingUserWithRegisteredCart() {
        givenApi()
            .pathParam("id", userWithCart.id())
        .when()
            .delete("/usuarios/{id}")
        .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/users-validation-error.schema.json"))
            .body("message", equalTo("Não é permitido excluir usuário com carrinho cadastrado"));
    }

    @Test
    @DisplayName("Return a success message when removing a non-existent user")
    void shouldReturnSuccessWhenRemovingNonExistentUser() {
        String missingUserId = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 16);

        givenApi()
            .pathParam("id", missingUserId)
        .when()
            .delete("/usuarios/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/delete-user-success.schema.json"))
            .body("message", equalTo("Nenhum registro excluído"));
    }

    @Test
    @DisplayName("Return a success message when the user identifier is invalid")
    void shouldReturnSuccessWhenUserIdIsInvalid() {
        givenApi()
            .pathParam("id", "invalid-id-value")
        .when()
            .delete("/usuarios/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/delete-user-success.schema.json"))
            .body("message", equalTo("Nenhum registro excluído"));
    }

    @Test
    @DisplayName("Return a success message when a blank user identifier is provided")
    void shouldReturnSuccessWhenBlankUserIdIsProvided() {
        givenApi()
            .pathParam("id", " ")
        .when()
            .delete("/usuarios/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/delete-user-success.schema.json"))
            .body("message", equalTo("Nenhum registro excluído"));
    }

    @Test
    @DisplayName("Return a success message when a whitespace user identifier is provided")
    void shouldReturnSuccessWhenWhitespaceUserIdIsProvided() {
        givenApi()
            .pathParam("id", " ")
        .when()
            .delete("/usuarios/{id}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body(matchesJsonSchemaInClasspath("schemas/delete-user-success.schema.json"))
            .body("message", equalTo("Nenhum registro excluído"));
    }
}