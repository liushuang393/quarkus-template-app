// Copyright (c) 2024 Quarkus Template Project
//
// Licensed under the MIT License.
// See LICENSE file in the project root for full license information.

package com.example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** OpenAPI機能テスト */
@QuarkusTest
@TestProfile(OpenApiTest.TestProfile.class)
class OpenApiTest {

  public static class TestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      // 本番設定をベースとして、安全性のためだけにデータベースを変更
      return Map.of(
          "quarkus.datasource.db-kind", "h2",
          "quarkus.datasource.jdbc.url",
              "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
          "quarkus.datasource.username", "sa",
          "quarkus.datasource.password", "");
    }
  }

  @Test
  void testOpenApiEndpoint() {
    given()
        .when()
        .get("/q/openapi")
        .then()
        .statusCode(200)
        .contentType("application/yaml")
        .body(containsString("openapi: 3.1.0"))
        .body(containsString("title: quarkus-template-app API"));
  }

  @Test
  void testSwaggerUiEndpoint() {
    given().when().get("/q/swagger-ui").then().statusCode(200).contentType("text/html");
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
        .body("openapi", equalTo("3.1.0"))
        .body("info.title", equalTo("quarkus-template-app API"))
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
