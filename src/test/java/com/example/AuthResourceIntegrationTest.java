package com.example;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * 認証機能統合テスト
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthResourceIntegrationTest {

    @Test
    @Order(1)
    void testUserRegistration() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "testuser",
                    "password": "TestPass123",
                    "email": "test@example.com",
                    "role": "USER"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200)
            .body("message", equalTo("登録が完了しました"))
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
                    "username": "testuser",
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
                    "username": "testuser",
                    "password": "TestPass123"
                }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("user.username", equalTo("testuser"))
            .body("user.role", equalTo("USER"));
    }

    @Test
    @Order(4)
    void testUserLoginInvalidCredentials() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "testuser",
                    "password": "wrongpassword"
                }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401)
            .body("error", equalTo("認証に失敗しました"));
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
            .body("errorCode", equalTo("VALIDATION_ERROR"))
            .body("message", equalTo("入力データに不正があります"))
            .body("fieldErrors", notNullValue());
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
            .body("errorCode", equalTo("VALIDATION_ERROR"));
    }
}
