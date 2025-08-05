package com.example;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

/**
 * 認証機能統合テスト
 */
@QuarkusTest
@TestProfile(AuthResourceIntegrationTest.TestProfile.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthResourceIntegrationTest {

    public static class TestProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            // 本番設定をベースとして、安全性のためだけにデータベースを変更
            return Map.of(
                "quarkus.datasource.db-kind", "h2",
                "quarkus.datasource.jdbc.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "quarkus.datasource.username", "sa",
                "quarkus.datasource.password", ""
            );
        }
    }

    @Test
    @Order(1)
    void testUserRegistration() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "newtestuser",
                    "password": "TestPass123",
                    "email": "newtest@example.com",
                    "role": "USER"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200)
            .body("message", equalTo("登録が完了しました。ログインしてください。"))
            .body("userId", notNullValue());
    }

    @Test
    @Order(2)
    void testUserRegistrationDuplicate() {
        // 同じユーザー名で再度登録を試行
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "newtestuser",
                    "password": "TestPass123",
                    "email": "test2@example.com",
                    "role": "USER"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(400)
            .body("errorCode", equalTo("USER_ALREADY_EXISTS"))
            .body("message", equalTo("ユーザー名が既に存在します"));
    }

    @Test
    @Order(3)
    void testUserLogin() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "newtestuser",
                    "password": "TestPass123"
                }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("user.username", equalTo("newtestuser"))
            .body("user.role", equalTo("USER"));
    }

    @Test
    @Order(4)
    void testUserLoginInvalidCredentials() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "newtestuser",
                    "password": "wrongpassword"
                }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401)
            .body("errorCode", equalTo("AUTHENTICATION_FAILED"))
            .body("message", notNullValue());
    }

    @Test
    @Order(5)
    void testValidationErrors() {
        // 無効なデータでユーザー登録
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "ab",
                    "password": "123",
                    "email": "invalid-email",
                    "role": "USER"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(400)
            .body("title", equalTo("Constraint Violation"))
            .body("status", equalTo(400))
            .body("violations", notNullValue());
    }

    @Test
    @Order(6)
    void testEmptyRequestBody() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/auth/register")
        .then()
            .statusCode(400)
            .body("title", equalTo("Constraint Violation"))
            .body("status", equalTo(400));
    }
}
