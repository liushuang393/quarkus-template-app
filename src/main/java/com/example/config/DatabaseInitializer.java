// Copyright (c) 2024 Quarkus Template Project
//
// Licensed under the MIT License.
// See LICENSE file in the project root for full license information.

package com.example.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
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

/** データベース初期化クラス アプリケーション起動時にテーブル作成とデータ投入を行う */
@ApplicationScoped
public class DatabaseInitializer {

  private static final Logger LOG = Logger.getLogger(DatabaseInitializer.class);

  @Inject DataSource dataSource;

  @ConfigProperty(name = "quarkus.profile")
  String profile;

  void onStart(@Observes StartupEvent ev) {
    LOG.info("データベース初期化を開始します...");

    try {
      if ("dev".equals(profile) || "test".equals(profile)) {
        LOG.infof("プロファイル '%s' でデータベース初期化を実行します", profile);
        initializeDatabase("database-setup-h2.sql");
      } else if ("prod".equals(profile)) {
        // 本番環境では手動でのスキーマ管理を推奨
        LOG.info("本番環境のため、データベース初期化をスキップします");
      } else {
        // その他の環境（デフォルトでH2を使用）
        LOG.infof("プロファイル '%s' でデータベース初期化を実行します", profile);
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
        // 改良されたSQL分割処理
        String[] statements = splitSqlStatements(sql);
        for (String stmt : statements) {
          String trimmedStmt = stmt.trim();
          if (!trimmedStmt.isEmpty() && !trimmedStmt.startsWith("--")) {
            LOG.infof(
                "SQL実行: %s", trimmedStmt.substring(0, Math.min(100, trimmedStmt.length())) + "...");
            try {
              statement.execute(trimmedStmt);
              LOG.debugf("SQL実行成功: %s", trimmedStmt);
            } catch (SQLException e) {
              // インデックス作成エラーなどは警告として処理
              if (trimmedStmt.toUpperCase().contains("CREATE INDEX")) {
                LOG.warnf("インデックス作成をスキップ: %s - %s", trimmedStmt, e.getMessage());
              } else {
                LOG.errorf("SQL実行エラー: %s - %s", trimmedStmt, e.getMessage());
                throw e;
              }
            }
          }
        }
      }

      LOG.info("SQLスクリプト実行完了: " + scriptFile);
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
        throw new RuntimeException("SQLスクリプトが見つかりません: " + filename);
      }

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
        return reader.lines().collect(Collectors.joining("\n"));
      }
    }
  }
}
