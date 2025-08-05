# データベースセットアップガイド

## ⚠️ **重要なセキュリティ警告**

> **🔴 本番環境で使用する前に必ず以下を変更してください！**
>
> ### 📋 **デフォルト認証情報（開発・テスト用のみ）**
>
> | ユーザー | パスワード | ロール | メール |
> |---------|-----------|--------|--------|
> | admin | AdminPass123 | ADMIN | admin@example.com |
> | sales | SalesPass123 | SALES | sales@example.com |
> | user | UserPass123 | USER | user@example.com |
> | testuser | Password123 | USER | test@example.com |
> | demouser | DemoPass123 | USER | demo@example.com |
>
> **これらの認証情報は本番環境では絶対に使用しないでください！**
>
> ### 🔧 **本番環境での必須変更項目**
> 1. すべてのデフォルトユーザー名・パスワードの変更
> 2. 強力なパスワードポリシーの適用
> 3. 実際のメールアドレスへの変更
> 4. 不要なテストユーザーの削除

## 概要

このドキュメントでは、Quarkus認証・権限管理システムのデータベースセットアップ手順を説明します。

## ファイル構成

```
quarkus-template-app/
├── database-setup.sql        # PostgreSQL用DDL（本番環境）
├── database-setup-h2.sql     # H2用DDL（開発環境）
└── DATABASE_SETUP.md         # このファイル
```

## 環境別セットアップ

### 🔧 開発環境（H2データベース）

開発環境では、H2インメモリデータベースを使用します。

#### 自動セットアップ

```bash
# 開発モードで起動（自動的にテーブル作成・データ投入）
./mvnw quarkus:dev -Pdev
```

#### 手動セットアップ

```bash
# H2コンソールにアクセス
# URL: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:testdb
# User: sa
# Password: (空白)

# SQLファイル実行
# database-setup-h2.sql の内容をコピー&ペースト
```

### 🚀 本番環境（PostgreSQL）

#### 1. PostgreSQLインストール

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib

# CentOS/RHEL
sudo yum install postgresql-server postgresql-contrib
sudo postgresql-setup initdb
sudo systemctl start postgresql
sudo systemctl enable postgresql

# macOS
brew install postgresql
brew services start postgresql

# Docker
docker run --name postgres-auth \
  -e POSTGRES_DB=quarkus_auth \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 -d postgres:15
```

#### 2. データベース作成

```bash
# PostgreSQLに接続
sudo -u postgres psql

# データベース作成
CREATE DATABASE quarkus_auth
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'ja_JP.UTF-8'
    LC_CTYPE = 'ja_JP.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

# データベースに接続
\c quarkus_auth;
```

#### 3. DDL実行

```bash
# SQLファイル実行
psql -h localhost -U postgres -d quarkus_auth -f database-setup.sql
```

#### 4. アプリケーション用ユーザー作成（推奨）

```sql
-- アプリケーション専用ユーザー作成
CREATE USER quarkus_app WITH PASSWORD 'your_secure_password_here';

-- 権限付与
GRANT CONNECT ON DATABASE quarkus_auth TO quarkus_app;
GRANT USAGE ON SCHEMA public TO quarkus_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO quarkus_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO quarkus_app;

-- 将来のテーブルに対する権限付与
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO quarkus_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO quarkus_app;
```

## データベーススキーマ

### テーブル構成

#### users テーブル

| カラム名   | データ型     | 制約             | 説明                         |
| ---------- | ------------ | ---------------- | ---------------------------- |
| id         | BIGSERIAL    | PRIMARY KEY      | ユーザーID                   |
| username   | VARCHAR(50)  | NOT NULL, UNIQUE | ユーザー名                   |
| password   | VARCHAR(255) | NOT NULL         | パスワード（BCryptハッシュ） |
| email      | VARCHAR(100) | NOT NULL         | メールアドレス               |
| role       | VARCHAR(20)  | NOT NULL         | ロール（ADMIN/USER/SALES）   |
| created_at | TIMESTAMP    | NOT NULL         | 作成日時                     |
| is_active  | BOOLEAN      | NOT NULL         | アクティブフラグ             |

#### audit_logs テーブル

| カラム名      | データ型     | 制約        | 説明                 |
| ------------- | ------------ | ----------- | -------------------- |
| id            | BIGSERIAL    | PRIMARY KEY | ログID               |
| user_id       | BIGINT       | FOREIGN KEY | ユーザーID           |
| username      | VARCHAR(50)  | NOT NULL    | ユーザー名           |
| action        | VARCHAR(100) | NOT NULL    | アクション           |
| resource_type | VARCHAR(50)  |             | リソースタイプ       |
| resource_id   | VARCHAR(100) |             | リソースID           |
| details       | TEXT         |             | 詳細情報             |
| ip_address    | VARCHAR(45)  |             | IPアドレス           |
| user_agent    | TEXT         |             | ユーザーエージェント |
| request_id    | VARCHAR(100) |             | リクエストID         |
| status        | VARCHAR(20)  | NOT NULL    | ステータス           |
| error_message | TEXT         |             | エラーメッセージ     |
| created_at    | TIMESTAMP    | NOT NULL    | 作成日時             |

## 初期データ

### 初期ユーザー

| ユーザー名 | パスワード   | ロール | 用途         |
| ---------- | ------------ | ------ | ------------ |
| admin      | AdminPass123 | ADMIN  | 管理者       |
| sales      | SalesPass123 | SALES  | 営業担当     |
| user       | UserPass123  | USER   | 一般ユーザー |
| testuser   | Password123  | USER   | テスト用     |
| demouser   | DemoPass123  | USER   | デモ用       |

### パスワードハッシュ

すべてのパスワードはBCryptでハッシュ化されています：

```
$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0qQZpVy6KI1TK.rS.8xO2T6.S.S2
```

## メンテナンス

### バックアップ

```bash
# データベース全体のバックアップ
pg_dump -h localhost -U postgres -d quarkus_auth > quarkus_auth_backup.sql

# データのみバックアップ
pg_dump -h localhost -U postgres -d quarkus_auth --data-only > quarkus_auth_data.sql
```

### リストア

```bash
# データベースリストア
psql -h localhost -U postgres -d quarkus_auth < quarkus_auth_backup.sql
```

### 監査ログクリーンアップ

```sql
-- 90日以上前の監査ログ削除
DELETE FROM audit_logs WHERE created_at < CURRENT_DATE - INTERVAL '90 days';
```

## トラブルシューティング

### よくある問題

#### 1. 接続エラー

```bash
# PostgreSQL接続確認
psql -h localhost -U postgres -d quarkus_auth

# 接続設定確認
cat /etc/postgresql/*/main/pg_hba.conf
```

#### 2. 権限エラー

```sql
-- ユーザー権限確認
\du

-- テーブル権限確認
\dp
```

#### 3. 文字化け

```sql
-- データベース文字コード確認
SELECT datname, datcollate, datctype FROM pg_database WHERE datname = 'quarkus_auth';
```

## セキュリティ考慮事項

### 本番環境での注意点

1. **強力なパスワード設定**
   - データベースユーザーのパスワードを変更
   - 初期ユーザーのパスワードを変更

2. **ネットワークセキュリティ**
   - PostgreSQLのアクセス制限設定
   - ファイアウォール設定

3. **定期メンテナンス**
   - 監査ログの定期削除
   - データベースバックアップ
   - セキュリティアップデート

### パスワード変更例

```sql
-- 初期ユーザーのパスワード変更
UPDATE users SET password = '$2a$10$NEW_HASH_HERE' WHERE username = 'admin';
```

## 監視・パフォーマンス

### 有用なクエリ

```sql
-- テーブルサイズ確認
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- アクティブユーザー数
SELECT role, COUNT(*) FROM users WHERE is_active = true GROUP BY role;

-- 監査ログ統計
SELECT action, status, COUNT(*) FROM audit_logs GROUP BY action, status;
```

---

## サポート

問題が発生した場合は、以下を確認してください：

1. アプリケーションログ
2. データベースログ
3. 設定ファイル（application-\*.yaml）
4. ネットワーク接続

詳細なトラブルシューティングについては、README.mdを参照してください。
