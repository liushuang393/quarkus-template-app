// Copyright (c) 2024 Quarkus Template Project
//
// Licensed under the MIT License.
// See LICENSE file in the project root for full license information.

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

/** データベース初期化クラス アプリケーション起動時にテーブル作成とデータ投入を行う */
@ApplicationScoped
public class DatabaseInitializer {

  private static final Logger LOG = Logger.getLogger(DatabaseInitializer.class);

  @Inject DataSource dataSource;

  @ConfigProperty(name = "quarkus.profile")
  String profile;

  void onStart(@Observes StartupEvent ev) {
    LOG.info("Starting database initialization...");

    try {
      if ("dev".equals(profile) || "test".equals(profile)) {
        LOG.infof("Executing database initialization for profile '%s'", profile);
        initializeDatabase("database-setup-h2.sql");
      } else if ("prod".equals(profile)) {
        // Manual schema management is recommended for production
        LOG.info("Skipping database initialization for production environment");
      } else {
        // Other environments (using H2 by default)
        LOG.infof("Executing database initialization for profile '%s'", profile);
        initializeDatabase("database-setup-h2.sql");
      }

      LOG.info("Database initialization completed successfully");

    } catch (Exception e) {
      LOG.error("Database initialization failed", e);
      throw new RuntimeException("Database initialization error", e);
    }
  }

  private void initializeDatabase(String scriptFile) throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      // SQLスクリプトを読み込み
      String sql = loadSqlScript(scriptFile);

      // SQLを実行
      try (Statement statement = connection.createStatement()) {
        // 改良されたSQL分割処理
        String[] statements = splitSqlStatements(sql);
        for (String stmt : statements) {
          String trimmedStmt = stmt.trim();
          if (!trimmedStmt.isEmpty() && !trimmedStmt.startsWith("--")) {
            LOG.infof(
                "Executing SQL: %s", trimmedStmt.substring(0, Math.min(100, trimmedStmt.length())) + "...");
            try {
              statement.execute(trimmedStmt);
              LOG.debugf("SQL execution successful: %s", trimmedStmt);
            } catch (SQLException e) {
              // Handle index creation errors as warnings
              if (trimmedStmt.toUpperCase().contains("CREATE INDEX")) {
                LOG.warnf("Skipping index creation: %s - %s", trimmedStmt, e.getMessage());
              } else {
                LOG.errorf("SQL execution error: %s - %s", trimmedStmt, e.getMessage());
                throw e;
              }
            }
          }
        }
      }

      LOG.info("SQL script execution completed: " + scriptFile);
    }
  }

  private String[] splitSqlStatements(String sql) {
    // コメント行を除去
    String[] lines = sql.split("\n");
    StringBuilder cleanSql = new StringBuilder();

    for (String line : lines) {
      String trimmedLine = line.trim();
      // コメント行をスキップ
      if (!trimmedLine.startsWith("--") && !trimmedLine.isEmpty()) {
        cleanSql.append(line).append("\n");
      }
    }

    // セミコロンで分割（ただし、文字列内のセミコロンは除外）
    return cleanSql.toString().split("(?<!\\w);(?!\\w)");
  }

  private String loadSqlScript(String filename) throws Exception {
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename)) {
      if (inputStream == null) {
        throw new RuntimeException("SQL script not found: " + filename);
      }

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
        return reader.lines().collect(Collectors.joining("\n"));
      }
    }
  }
}
