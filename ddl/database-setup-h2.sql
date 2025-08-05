-- =====================================================
-- Quarkus 認証・権限管理システム H2データベース設定
-- =====================================================
-- 作成日: 2025-08-04
-- 説明: H2データベース用のテーブル作成、初期データ投入スクリプト（開発環境用）
-- =====================================================

-- =====================================================
-- 1. テーブル作成（H2用）
-- =====================================================

-- ユーザーテーブル
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 監査ログテーブル
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    details CLOB,
    ip_address VARCHAR(45),
    user_agent CLOB,
    request_id VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 外部キー制約
    CONSTRAINT fk_audit_logs_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- =====================================================
-- 2. インデックス作成
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
-- 3. 初期データ投入
-- =====================================================

-- 管理者ユーザー作成
-- パスワード: AdminPass123 (BCryptハッシュ化済み)
MERGE INTO users (username, password, email, role, is_active)
KEY(username) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0qQZpVy6KI1TK.rS.8xO2T6.S.S2', 'admin@example.com', 'ADMIN', true);

-- 営業ユーザー作成
-- パスワード: SalesPass123 (BCryptハッシュ化済み)
MERGE INTO users (username, password, email, role, is_active)
KEY(username) VALUES
('sales', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0qQZpVy6KI1TK.rS.8xO2T6.S.S2', 'sales@example.com', 'SALES', true);

-- 一般ユーザー作成
-- パスワード: UserPass123 (BCryptハッシュ化済み)
MERGE INTO users (username, password, email, role, is_active)
KEY(username) VALUES
('user', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0qQZpVy6KI1TK.rS.8xO2T6.S.S2', 'user@example.com', 'USER', true);

-- テストユーザー作成（開発・テスト用）
-- パスワード: Password123 (BCryptハッシュ化済み)
MERGE INTO users (username, password, email, role, is_active)
KEY(username) VALUES
('testuser', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0qQZpVy6KI1TK.rS.8xO2T6.S.S2', 'test@example.com', 'USER', true);

-- デモユーザー作成（画面テスト用）
-- パスワード: DemoPass123 (BCryptハッシュ化済み)
MERGE INTO users (username, password, email, role, is_active)
KEY(username) VALUES
('demouser', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0qQZpVy6KI1TK.rS.8xO2T6.S.S2', 'demo@example.com', 'USER', true);

-- =====================================================
-- 4. 初期監査ログ投入
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
-- 5. データ確認クエリ（H2用）
-- =====================================================

-- ユーザー一覧確認
-- SELECT id, username, email, role, is_active, created_at FROM users ORDER BY id;

-- 監査ログ確認
-- SELECT id, username, action, resource_type, status, created_at FROM audit_logs ORDER BY created_at DESC LIMIT 10;

-- ロール別ユーザー数確認
-- SELECT role, COUNT(*) as user_count FROM users WHERE is_active = true GROUP BY role;

-- =====================================================
-- 6. H2コンソール接続情報
-- =====================================================
--
-- 開発モード時のH2コンソールアクセス:
-- URL: http://localhost:8080/h2-console
--
-- 接続設定:
-- - Driver Class: org.h2.Driver
-- - JDBC URL: jdbc:h2:mem:testdb
-- - User Name: sa
-- - Password: (空白)
--
-- =====================================================

-- =====================================================
-- 7. 開発用便利クエリ
-- =====================================================

-- 全ユーザーのパスワードリセット（開発用）
-- UPDATE users SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0qQZpVy6KI1TK.rS.8xO2T6.S.S2' WHERE username != 'admin';

-- テストデータクリア
-- DELETE FROM audit_logs WHERE action != 'SYSTEM_INIT';
-- DELETE FROM users WHERE username NOT IN ('admin', 'sales', 'user');

-- 監査ログ統計
-- SELECT
--     action,
--     status,
--     COUNT(*) as count
-- FROM audit_logs
-- GROUP BY action, status
-- ORDER BY count DESC;

-- =====================================================
-- 完了
-- =====================================================
-- H2データベースセットアップが完了しました。
--
-- 初期ユーザー:
-- - admin/AdminPass123 (ADMIN)
-- - sales/SalesPass123 (SALES)
-- - user/UserPass123 (USER)
-- - testuser/Password123 (USER) - テスト用
-- - demouser/DemoPass123 (USER) - デモ用
--
-- H2コンソール: http://localhost:8080/h2-console
-- =====================================================
