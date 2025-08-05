package com.example.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * データベース初期化クラス
 * アプリケーション起動時にテーブル作成とデータ投入を行う
 */
@ApplicationScoped
public class DatabaseInitializer {
    
    private static final Logger LOG = Logger.getLogger(DatabaseInitializer.class);
    
    @Inject
    DataSource dataSource;
    
    @ConfigProperty(name = "quarkus.profile")
    String profile;
    
    void onStart(@Observes StartupEvent ev) {
        LOG.info("データベース初期化を開始します...");
        
        try {
            if ("dev".equals(profile)) {
                initializeDatabase("database-setup-h2.sql");
            } else if ("prod".equals(profile)) {
                // 本番環境では手動でのスキーマ管理を推奨
                LOG.info("本番環境のため、データベース初期化をスキップします");
            } else {
                // テスト環境
                initializeDatabase("database-setup-h2.sql");
            }
            
            LOG.info("データベース初期化が完了しました");
            
        } catch (Exception e) {
            LOG.error("データベース初期化に失敗しました", e);
            throw new RuntimeException("データベース初期化エラー", e);
        }
    }
    
    private void initializeDatabase(String scriptFile) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            // SQLスクリプトを読み込み
            String sql = loadSqlScript(scriptFile);
            
            // SQLを実行
            try (Statement statement = connection.createStatement()) {
                // セミコロンで分割して個別に実行
                String[] statements = sql.split(";");
                for (String stmt : statements) {
                    String trimmedStmt = stmt.trim();
                    if (!trimmedStmt.isEmpty() && !trimmedStmt.startsWith("--")) {
                        LOG.debugf("SQL実行: %s", trimmedStmt);
                        try {
                            statement.execute(trimmedStmt);
                        } catch (SQLException e) {
                            // インデックス作成エラーなどは警告として処理
                            if (trimmedStmt.toUpperCase().contains("CREATE INDEX")) {
                                LOG.warnf("インデックス作成をスキップ: %s - %s", trimmedStmt, e.getMessage());
                            } else {
                                throw e;
                            }
                        }
                    }
                }
            }
            
            LOG.info("SQLスクリプト実行完了: " + scriptFile);
        }
    }
    
    private String loadSqlScript(String filename) throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new RuntimeException("SQLスクリプトが見つかりません: " + filename);
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }
}
