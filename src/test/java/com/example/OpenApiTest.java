package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * OpenAPI機能テスト
 */
@QuarkusTest
class OpenApiTest {

    @Test
    void testOpenApiEndpoint() {
        given()
        .when()
            .get("/q/openapi")
        .then()
            .statusCode(200)
            .contentType("application/yaml")
            .body(containsString("openapi: 3.0.3"))
            .body(containsString("title: Quarkus 認証・権限管理システム API"));
    }

    @Test
    void testSwaggerUiEndpoint() {
        given()
        .when()
            .get("/q/swagger-ui")
        .then()
            .statusCode(200)
            .contentType("text/html");
    }

    @Test
    void testOpenApiJsonFormat() {
        given()
            .header("Accept", "application/json")
        .when()
            .get("/q/openapi")
        .then()
            .statusCode(200)
            .contentType("application/json")
            .body("openapi", equalTo("3.0.3"))
            .body("info.title", equalTo("Quarkus 認証・権限管理システム API"))
            .body("paths", notNullValue())
            .body("paths.'/auth/register'", notNullValue())
            .body("paths.'/auth/login'", notNullValue());
    }

    @Test
    void testApiDocumentation() {
        given()
            .header("Accept", "application/json")
        .when()
            .get("/q/openapi")
        .then()
            .statusCode(200)
            .body("paths.'/auth/register'.post.summary", equalTo("ユーザー登録"))
            .body("paths.'/auth/login'.post.summary", equalTo("ログイン"))
            .body("tags.find { it.name == '認証' }.description", equalTo("ユーザー認証関連のAPI"));
    }
}
