package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * メトリクス機能テスト
 */
@QuarkusTest
class MetricsTest {

    @Test
    void testMetricsEndpoint() {
        given()
        .when()
            .get("/q/metrics")
        .then()
            .statusCode(200)
            .contentType("text/plain")
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
