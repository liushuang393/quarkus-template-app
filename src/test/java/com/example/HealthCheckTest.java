package com.example;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import static io.restassured.RestAssured.given;

/**
 * ヘルスチェック機能テスト
 */
@QuarkusTest
@TestProfile(HealthCheckTest.TestProfile.class)
class HealthCheckTest {

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
