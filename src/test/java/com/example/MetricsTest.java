package com.example;

import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import static io.restassured.RestAssured.given;

/**
 * メトリクス機能テスト
 */
@QuarkusTest
@TestProfile(MetricsTest.TestProfile.class)
class MetricsTest {

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
    void testMetricsEndpoint() {
        given()
        .when()
            .get("/q/metrics")
        .then()
            .statusCode(200)
            .contentType(containsString("application/openmetrics-text"))
            .body(containsString("# HELP"))
            .body(containsString("# TYPE"));
    }

    @Test
    void testPrometheusMetrics() {
        given()
        .when()
            .get("/q/metrics")
        .then()
            .statusCode(200)
            .body(containsString("jvm_"))
            .body(containsString("http_"));
    }

    @Test
    void testApplicationMetrics() {
        // まずAPIを呼び出してメトリクスを生成
        given()
            .contentType("application/json")
            .body("""
                {
                    "username": "metricstest",
                    "password": "TestPass123",
                    "email": "metrics@example.com",
                    "role": "USER"
                }
                """)
        .when()
            .post("/auth/register");

        // メトリクスを確認
        given()
        .when()
            .get("/q/metrics")
        .then()
            .statusCode(200)
            .body(containsString("http_server_requests"));
    }
}
