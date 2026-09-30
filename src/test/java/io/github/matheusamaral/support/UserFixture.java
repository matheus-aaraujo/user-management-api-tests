package io.github.matheusamaral.support;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.matheusamaral.support.ApiRequest.givenApi;
import static org.junit.jupiter.api.Assertions.assertAll;

public final class UserFixture {

    public record User(String id, String name, String email, String password,
                       String administrator) {
    }

    public record Product(String id, String name) {
    }

    private User regularUser;
    private User administratorUser;
    private User productAdministrator;
    private final List<String> createdUserIds = new ArrayList<>();
    private final List<String> createdProductIds = new ArrayList<>();
    private final List<String> cartOwnerTokens = new ArrayList<>();

    public void create() {
        String unique = UUID.randomUUID().toString();
        regularUser = createUser("Listing test " + unique,
                "listing-" + unique + "@example.com", "Test-" + unique, "false");
        administratorUser = createUser("Administrator " + unique,
                "admin-" + unique + "@example.com", "AdminSecret-" + unique, "true");
    }

    public User regularUser() {
        return regularUser;
    }

    public User administratorUser() {
        return administratorUser;
    }

    public User createUser(String name, String email, String password, String administrator) {
        String requestBody = """
                {
                  "nome": "%s",
                  "email": "%s",
                  "password": "%s",
                  "administrador": "%s"
                }
                """.formatted(name, email, password, administrator);

        String id = givenApi()
                .contentType(ContentType.JSON)
                .body(requestBody)
            .when()
                .post("/usuarios")
            .then()
                .statusCode(201)
                .extract().path("_id");

        if (id == null || id.isBlank()) {
            throw new AssertionError("User creation did not return an ID");
        }
        createdUserIds.add(id);
        return new User(id, name, email, password, administrator);
    }

    public Product createProduct(User administrator) {
        String unique = UUID.randomUUID().toString();
        String productName = "Cart restriction product " + unique;
        String requestBody = """
                {
                  "nome": "%s",
                  "preco": 100,
                  "descricao": "Product used to create a cart",
                  "quantidade": 1
                }
                """.formatted(productName);

        String id = givenApi()
            .contentType(ContentType.JSON)
            .header("Authorization", login(administrator))
            .body(requestBody)
        .when()
            .post("/produtos")
        .then()
            .statusCode(201)
            .extract().path("_id");

        if (id == null || id.isBlank()) {
            throw new AssertionError("Product creation did not return an ID");
        }
        productAdministrator = administrator;
        createdProductIds.add(id);
        return new Product(id, productName);
    }

    public void createCart(User user, Product product) {
        String token = login(user);
        String requestBody = """
                {
                  "produtos": [
                    {
                      "idProduto": "%s",
                      "quantidade": 1
                    }
                  ]
                }
                """.formatted(product.id());

        givenApi()
            .contentType(ContentType.JSON)
            .header("Authorization", token)
            .body(requestBody)
        .when()
            .post("/carrinhos")
        .then()
            .statusCode(201);

        cartOwnerTokens.add(token);
    }

    public void deleteUser(String id) {
        givenApi()
            .pathParam("id", id)
        .when()
            .delete("/usuarios/{id}")
        .then()
            .statusCode(200);
        createdUserIds.remove(id);
    }

    private String login(User user) {
        String requestBody = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(user.email(), user.password());

        return givenApi()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/login")
        .then()
            .statusCode(200)
            .extract().path("authorization");
    }

    private void cancelCart(String token) {
        givenApi()
            .header("Authorization", token)
        .when()
            .delete("/carrinhos/cancelar-compra")
        .then()
            .statusCode(200);
    }

    private void deleteProduct(String id, User administrator) {
        givenApi()
            .header("Authorization", login(administrator))
            .pathParam("id", id)
        .when()
            .delete("/produtos/{id}")
        .then()
            .statusCode(200);
    }

    public void cleanup() {
        List<Executable> cleanups = new ArrayList<>();
        for (String token : List.copyOf(cartOwnerTokens)) {
            cleanups.add(() -> {
                cancelCart(token);
                cartOwnerTokens.remove(token);
            });
        }
        for (String id : List.copyOf(createdProductIds)) {
            cleanups.add(() -> {
                deleteProduct(id, productAdministrator);
                createdProductIds.remove(id);
            });
        }
        for (String id : List.copyOf(createdUserIds)) {
            cleanups.add(() -> deleteUser(id));
        }
        try {
            assertAll("Delete fixture data", cleanups);
        } finally {
            regularUser = null;
            administratorUser = null;
            productAdministrator = null;
        }
    }
}