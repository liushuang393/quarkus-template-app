# 本番環境セキュリティ設定ガイド

## ⚠️ **重要：本番環境デプロイ前の必須チェックリスト**

このガイドでは、本番環境にデプロイする前に必ず実施すべきセキュリティ設定について説明します。

## 🔴 **緊急度：高**

### 1. **デフォルト認証情報の変更**

#### 📋 **現在のデフォルト認証情報**
```
⚠️ これらは開発・テスト用のみです！本番では絶対に使用禁止！

管理者アカウント:
- ユーザー名: admin
- パスワード: AdminPass123
- メール: admin@example.com

営業アカウント:
- ユーザー名: sales
- パスワード: SalesPass123
- メール: sales@example.com

一般ユーザーアカウント:
- ユーザー名: user
- パスワード: UserPass123
- メール: user@example.com

テストアカウント:
- ユーザー名: testuser
- パスワード: Password123
- メール: test@example.com

デモアカウント:
- ユーザー名: demouser
- パスワード: DemoPass123
- メール: demo@example.com
```

#### 🔧 **変更手順**

1. **DDLファイルの編集**
   ```sql
   -- ddl/database-setup.sql を編集

   -- 強力なパスワードに変更（BCryptハッシュ化）
   INSERT INTO users (username, password, email, role, is_active) VALUES
   ('your_admin_user', '$2a$10$YOUR_STRONG_BCRYPT_HASH', 'your-admin@your-domain.com', 'ADMIN', true);
   ```

2. **パスワードハッシュ生成**
   ```bash
   # BCryptハッシュ生成（Java）
   String hashedPassword = BCrypt.hashpw("YourStrongPassword123!", BCrypt.gensalt(12));
   ```

3. **不要なアカウントの削除**
   - テストユーザー（testuser）
   - デモユーザー（demouser）
   - 使用しない営業アカウント

### 2. **JWT設定の変更**

#### 🔑 **JWT秘密鍵の変更**
```yaml
# src/main/resources/application-prod.yaml

mp:
  jwt:
    verify:
      publickey:
        location: META-INF/resources/publicKey.pem
      issuer: https://your-domain.com
    decrypt:
      key:
        location: META-INF/resources/privateKey.pem

# 強力な秘密鍵を生成して設定
smallrye:
  jwt:
    sign:
      key: "YOUR_VERY_STRONG_SECRET_KEY_HERE_AT_LEAST_256_BITS"
    new-token:
      lifespan: 3600  # 1時間（本番環境では短めに設定）
```

#### 🔐 **RSAキーペア生成**
```bash
# 秘密鍵生成
openssl genrsa -out privateKey.pem 2048

# 公開鍵生成
openssl rsa -pubout -in privateKey.pem -out publicKey.pem
```

### 3. **データベース認証情報**

#### 🗄️ **PostgreSQL設定**
```yaml
# application-prod.yaml

quarkus:
  datasource:
    db-kind: postgresql
    username: ${DB_USERNAME:your_db_user}
    password: ${DB_PASSWORD:your_strong_db_password}
    jdbc:
      url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:your_db_name}
      max-size: 16
      min-size: 2
```

#### 🔒 **環境変数での設定**
```bash
# 環境変数で機密情報を管理
export DB_USERNAME="your_production_db_user"
export DB_PASSWORD="your_very_strong_db_password"
export DB_HOST="your-db-server.com"
export DB_NAME="your_production_db"
export JWT_SECRET="your_256_bit_jwt_secret_key"
```

## 🟡 **緊急度：中**

### 4. **CORS設定の制限**

```yaml
# 本番環境では特定のドメインのみ許可
quarkus:
  http:
    cors:
      origins: "https://your-frontend-domain.com,https://your-admin-panel.com"
      headers: "accept, authorization, content-type, x-requested-with"
      methods: "GET,PUT,POST,DELETE"
```

### 5. **ログ設定の調整**

```yaml
# 本番環境では機密情報をログに出力しない
quarkus:
  log:
    level: WARN
    category:
      "com.example": INFO
      "com.example.service.JwtService": WARN  # JWT詳細をログに出さない
      "com.example.mapper": WARN              # SQL詳細をログに出さない
```

### 6. **セッション設定**

```yaml
# セッションタイムアウトの設定
quarkus:
  http:
    auth:
      session:
        timeout: 30M  # 30分でタイムアウト
```

## 🟢 **緊急度：低（推奨）**

### 7. **HTTPS強制**

```yaml
# HTTPS リダイレクト
quarkus:
  http:
    insecure-requests: redirect  # HTTPをHTTPSにリダイレクト
```

### 8. **レート制限**

```yaml
# API レート制限（拡張機能が必要）
quarkus:
  rate-limiter:
    enabled: true
    default-config:
      limit: 100
      period: 1M
```

## 📋 **デプロイ前チェックリスト**

- [ ] すべてのデフォルトユーザー名・パスワードを変更
- [ ] JWT秘密鍵を強力なものに変更
- [ ] データベース認証情報を本番用に変更
- [ ] 不要なテストアカウントを削除
- [ ] CORS設定を本番ドメインに制限
- [ ] ログレベルを本番用に調整
- [ ] HTTPS設定を有効化
- [ ] 環境変数で機密情報を管理
- [ ] セキュリティテストの実施
- [ ] 脆弱性スキャンの実施

## 🚨 **緊急時の対応**

### セキュリティインシデント発生時
1. 即座にサービスを停止
2. 影響範囲の調査
3. ログの保全
4. パスワード・トークンの無効化
5. セキュリティパッチの適用
6. サービス再開

### 定期的なセキュリティメンテナンス
- 月次：依存関係の脆弱性チェック
- 四半期：パスワードポリシーの見直し
- 半年：JWT秘密鍵のローテーション
- 年次：セキュリティ監査の実施

## 📞 **サポート**

セキュリティに関する質問や問題が発生した場合は、開発チームまでお問い合わせください。

**重要：セキュリティ問題は最優先で対応します。**
