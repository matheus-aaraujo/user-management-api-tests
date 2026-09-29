package io.github.matheusamaral.support;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.matheusamaral.support.ApiRequest.givenApi;
import static org.junit.jupiter.api.Assertions.assertAll;

/** Owns the users created for a read-only test suite. */
public final class UserFixture {

    public record User(String id, String name, String email, String password,
                       String administrator) {
    }

    private User regularUser;
    private User administratorUser;
    private final List<String> createdUserIds = new ArrayList<>();

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

    private User createUser(String name, String email, String password, String administrator) {
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

    public void deleteUser(String id) {
        givenApi()
            .pathParam("id", id)
        .when()
            .delete("/usuarios/{id}")
        .then()
            .statusCode(200);
    }

    public void cleanup() {
        List<Executable> deletions = new ArrayList<>();
        for (String id : List.copyOf(createdUserIds)) {
            deletions.add(() -> {
                deleteUser(id);
                createdUserIds.remove(id);
            });
        }
        try {
            assertAll("Delete fixture users", deletions);
        } finally {
            regularUser = null;
            administratorUser = null;
        }
    }
}