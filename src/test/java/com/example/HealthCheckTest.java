package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * ヘルスチェック機能テスト
 */
@QuarkusTest
class HealthCheckTest {

    @Test
    void testHealthEndpoint() {
        given()
        .when()
            .get("/q/health")
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .body("checks", notNullValue());
    }

    @Test
    void testReadinessEndpoint() {
        given()
        .when()
            .get("/q/health/ready")
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    @Test
    void testLivenessEndpoint() {
        given()
        .when()
            .get("/q/health/live")
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    @Test
    void testDatabaseHealthCheck() {
        given()
        .when()
            .get("/q/health/ready")
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .body("checks.find { it.name == 'Database connection' }.status", equalTo("UP"));
    }
}
