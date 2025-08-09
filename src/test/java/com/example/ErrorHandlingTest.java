// Copyright (c) 2024 Quarkus Template Project
//
// Licensed under the MIT License.
// See LICENSE file in the project root for full license information.

package com.example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * 错误处理机制测试
 * 验证业务异常能够正确传播到前端并显示具体错误信息
 */
@QuarkusTest
public class ErrorHandlingTest {

    @Test
    public void testUserAlreadyExistsError() {
        // 首先注册一个用户
        String requestBody = """
            {
                "username": "testuser",
                "password": "TestPass123",
                "email": "test@example.com",
                "role": "USER"
            }
            """;

        given()
            .contentType("application/json")
            .body(requestBody)
            .when()
            .post("/auth/register")
            .then()
            .statusCode(200);

        // 尝试注册相同用户名，应该返回具体的错误信息
        given()
            .contentType("application/json")
            .body(requestBody)
            .when()
            .post("/auth/register")
            .then()
            .statusCode(400)
            .body("errorCode", is("USER_ALREADY_EXISTS"))
            .body("message", is("Username already exists"))
            .body("path", is("/auth/register"));
    }

    @Test
    public void testValidationError() {
        // 测试验证错误
        String invalidRequestBody = """
            {
                "username": "ab",
                "password": "weak",
                "email": "invalid-email",
                "role": "USER"
            }
            """;

        given()
            .contentType("application/json")
            .body(invalidRequestBody)
            .when()
            .post("/auth/register")
            .then()
            .statusCode(400)
            .body("errorCode", is("VALIDATION_ERROR"))
            .body("message", notNullValue())
            .body("fieldErrors", notNullValue());
    }

    @Test
    public void testAuthenticationFailedError() {
        // 测试认证失败错误
        String loginRequest = """
            {
                "username": "nonexistent",
                "password": "wrongpassword"
            }
            """;

        given()
            .contentType("application/json")
            .body(loginRequest)
            .when()
            .post("/auth/login")
            .then()
            .statusCode(401)
            .body("errorCode", is("AUTHENTICATION_FAILED"))
            .body("message", is("Authentication failed"));
    }
}
