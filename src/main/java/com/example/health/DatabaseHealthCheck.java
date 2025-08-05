package com.example.health;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * データベース接続ヘルスチェック
 */
@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {
    
    @Inject
    DataSource dataSource;
    
    @Override
    public HealthCheckResponse call() {
        try (Connection connection = dataSource.getConnection()) {
            // 簡単なクエリでデータベース接続を確認
            boolean isValid = connection.isValid(5); // 5秒タイムアウト
            
            if (isValid) {
                return HealthCheckResponse.up("Database connection");
            } else {
                return HealthCheckResponse.down("Database connection");
            }
        } catch (SQLException e) {
            return HealthCheckResponse.down("Database connection");
        }
    }
}
