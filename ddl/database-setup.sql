-- =====================================================
-- Quarkus 認証・権限管理システム データベース設定
-- =====================================================
-- 作成日: 2025-08-04
-- 説明: PostgreSQL用のデータベース、テーブル作成、初期データ投入スクリプト
-- =====================================================
--
-- ⚠️  重要なセキュリティ警告 ⚠️
-- =====================================================
-- 🔴 本番環境で使用する前に必ず以下を変更してください！
--
-- 1. デフォルトユーザー名・パスワードの変更
-- 2. メールアドレスの変更
-- 3. 強力なパスワードの設定
--
-- このファイルに含まれるデフォルト認証情報：
-- - admin / AdminPass123
-- - sales / SalesPass123
-- - user / UserPass123
-- - testuser / Password123
-- - demouser / DemoPass123
--
-- これらは開発・テスト用のみです！
-- 本番環境では絶対に使用しないでください！
-- =====================================================

-- =====================================================
-- 1. データベース作成
-- =====================================================

-- データベース作成（PostgreSQL管理者権限で実行）
-- CREATE DATABASE quarkus_auth
--     WITH
--     OWNER = postgres
--     ENCODING = 'UTF8'
--     LC_COLLATE = 'ja_JP.UTF-8'
--     LC_CTYPE = 'ja_JP.UTF-8'
--     TABLESPACE = pg_default
--     CONNECTION LIMIT = -1;

-- データベースに接続
-- \c quarkus_auth;

-- =====================================================
-- 2. テーブル作成
-- =====================================================

-- ユーザーテーブル
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- インデックス
    CONSTRAINT users_username_unique UNIQUE (username),
    CONSTRAINT users_email_check CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'USER', 'SALES'))
);

-- 監査ログテーブル
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_id VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 外部キー制約
    CONSTRAINT fk_audit_logs_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,

    -- チェック制約
    CONSTRAINT audit_logs_status_check CHECK (status IN ('SUCCESS', 'FAILURE', 'ERROR'))
);

-- =====================================================
-- 3. インデックス作成
-- =====================================================

-- ユーザーテーブルのインデックス
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- 監査ログテーブルのインデックス
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_username ON audit_logs(username);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_resource_type ON audit_logs(resource_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_status ON audit_logs(status);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_request_id ON audit_logs(request_id);

-- =====================================================
-- 4. 初期データ投入
-- =====================================================

-- 管理者ユーザー作成
-- パスワード: AdminPass123 (BCryptハッシュ化済み)
INSERT INTO users (username, password, email, role, is_active) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0qQZpVy6KI1TK.rS.8xO2T6.S.S2', 'admin@example.com', 'ADMIN', true)
ON CONFLICT (username) DO NOTHING;

-- 営業ユーザー作成
-- パスワード: SalesPass123 (BCryptハッシュ化済み)
INSERT INTO users (username, password, email, role, is_active) VALUES
('sales', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0qQZpVy6KI1TK.rS.8xO2T6.S.S2', 'sales@example.com', 'SALES', true)
ON CONFLICT (username) DO NOTHING;

-- 一般ユーザー作成
-- パスワード: UserPass123 (BCryptハッシュ化済み)
INSERT INTO users (username, password, email, role, is_active) VALUES
('user', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0qQZpVy6KI1TK.rS.8xO2T6.S.S2', 'user@example.com', 'USER', true)
ON CONFLICT (username) DO NOTHING;

-- テストユーザー作成（開発・テスト用）
-- パスワード: Password123 (BCryptハッシュ化済み)
INSERT INTO users (username, password, email, role, is_active) VALUES
('testuser', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0qQZpVy6KI1TK.rS.8xO2T6.S.S2', 'test@example.com', 'USER', true)
ON CONFLICT (username) DO NOTHING;

-- デモユーザー作成（画面テスト用）
-- パスワード: DemoPass123 (BCryptハッシュ化済み)
INSERT INTO users (username, password, email, role, is_active) VALUES
('demouser', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0qQZpVy6KI1TK.rS.8xO2T6.S.S2', 'demo@example.com', 'USER', true)
ON CONFLICT (username) DO NOTHING;

-- =====================================================
-- 5. 初期監査ログ投入
-- =====================================================

-- システム初期化ログ
INSERT INTO audit_logs (user_id, username, action, resource_type, details, status) VALUES
(1, 'admin', 'SYSTEM_INIT', 'SYSTEM', 'システム初期化完了', 'SUCCESS');

-- ユーザー作成ログ
INSERT INTO audit_logs (user_id, username, action, resource_type, resource_id, details, status) VALUES
(1, 'admin', 'USER_CREATE', 'USER', '1', '管理者ユーザー作成', 'SUCCESS'),
(1, 'admin', 'USER_CREATE', 'USER', '2', '営業ユーザー作成', 'SUCCESS'),
(1, 'admin', 'USER_CREATE', 'USER', '3', '一般ユーザー作成', 'SUCCESS'),
(1, 'admin', 'USER_CREATE', 'USER', '4', 'テストユーザー作成', 'SUCCESS'),
(1, 'admin', 'USER_CREATE', 'USER', '5', 'デモユーザー作成', 'SUCCESS');

-- =====================================================
-- 6. 権限設定
-- =====================================================

-- アプリケーション用ユーザー作成（本番環境用）
-- CREATE USER quarkus_app WITH PASSWORD 'your_secure_password_here';

-- 権限付与
-- GRANT CONNECT ON DATABASE quarkus_auth TO quarkus_app;
-- GRANT USAGE ON SCHEMA public TO quarkus_app;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO quarkus_app;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO quarkus_app;

-- 将来のテーブルに対する権限付与
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO quarkus_app;
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO quarkus_app;

-- =====================================================
-- 7. データ確認クエリ
-- =====================================================

-- ユーザー一覧確認
-- SELECT id, username, email, role, is_active, created_at FROM users ORDER BY id;

-- 監査ログ確認
-- SELECT id, username, action, resource_type, status, created_at FROM audit_logs ORDER BY created_at DESC LIMIT 10;

-- ロール別ユーザー数確認
-- SELECT role, COUNT(*) as user_count FROM users WHERE is_active = true GROUP BY role;

-- =====================================================
-- 8. メンテナンス用クエリ
-- =====================================================

-- 古い監査ログ削除（90日以上前）
-- DELETE FROM audit_logs WHERE created_at < CURRENT_DATE - INTERVAL '90 days';

-- 非アクティブユーザー確認
-- SELECT username, email, created_at FROM users WHERE is_active = false;

-- パスワード更新（例：testuser）
-- UPDATE users SET password = '$2a$10$NEW_HASH_HERE' WHERE username = 'testuser';

-- =====================================================
-- 9. バックアップ・リストア用コマンド
-- =====================================================

-- データベースバックアップ
-- pg_dump -h localhost -U postgres -d quarkus_auth > quarkus_auth_backup.sql

-- データベースリストア
-- psql -h localhost -U postgres -d quarkus_auth < quarkus_auth_backup.sql

-- =====================================================
-- 10. パフォーマンス監視用クエリ
-- =====================================================

-- テーブルサイズ確認
-- SELECT
--     schemaname,
--     tablename,
--     pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
-- FROM pg_tables
-- WHERE schemaname = 'public'
-- ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- インデックス使用状況確認
-- SELECT
--     schemaname,
--     tablename,
--     indexname,
--     idx_scan,
--     idx_tup_read,
--     idx_tup_fetch
-- FROM pg_stat_user_indexes
-- ORDER BY idx_scan DESC;

-- =====================================================
-- 完了
-- =====================================================
-- データベースセットアップが完了しました。
--
-- 初期ユーザー:
-- - admin/AdminPass123 (ADMIN)
-- - sales/SalesPass123 (SALES)
-- - user/UserPass123 (USER)
-- - testuser/Password123 (USER) - テスト用
-- - demouser/DemoPass123 (USER) - デモ用
-- =====================================================
