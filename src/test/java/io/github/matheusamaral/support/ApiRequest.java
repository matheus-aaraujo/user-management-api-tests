package io.github.matheusamaral.support;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class ApiRequest {

    private ApiRequest() {
    }

    public static RequestSpecification givenApi() {
        return RestAssured.given()
                .baseUri(System.getProperty("base.url", "https://serverest.dev"))
                .accept(ContentType.JSON);
    }
}
