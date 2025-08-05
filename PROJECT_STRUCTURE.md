# 📁 プロジェクト構造説明

## 🏗️ 全体構造

```
quarkus-template-app/
├── 📄 README.md                    # プロジェクト概要・使用方法
├── 📄 pom.xml                      # Maven設定ファイル
├── 📄 Makefile                     # 開発用コマンド集
├── 📄 docker-compose.yml           # 本番環境用Docker構成
├── 📄 docker-compose.dev.yml       # 開発環境用Docker構成
├── 📄 .dockerignore                # Docker除外ファイル
├── 📄 .gitignore                   # Git除外ファイル
├── 📄 mvnw / mvnw.cmd              # Mavenラッパー
├── 📁 .mvn/                        # Maven設定
├── 📁 src/                         # ソースコード
├── 📁 docker/                      # Dockerファイル
├── 📁 ddl/                         # データベーススキーマ
├── 📁 target/                      # ビルド成果物
└── 📁 __temp_tests__/              # テスト用一時ファイル
```

## 📂 src/ ディレクトリ詳細

### src/main/java/com/example/
```
src/main/java/com/example/
├── 🔐 AuthResource.java            # 認証API
├── 🏠 MenuResource.java            # メニューAPI  
├── 🎯 PageController.java          # ページ表示コントローラー
├── 📁 dto/                         # データ転送オブジェクト
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── ErrorResponse.java
├── 📁 entity/                      # エンティティクラス
│   ├── User.java
│   └── AuditLog.java
├── 📁 service/                     # ビジネスロジック
│   ├── UserService.java
│   ├── JwtService.java
│   └── AuditLogService.java
├── 📁 exception/                   # 例外処理
│   └── GlobalExceptionHandler.java
├── 📁 interceptor/                 # インターセプター
│   └── AuditInterceptor.java
└── 📁 health/                      # ヘルスチェック
    └── DatabaseHealthCheck.java
```

### src/main/resources/
```
src/main/resources/
├── ⚙️ application.yaml             # 基本設定
├── ⚙️ application-dev.yaml         # 開発環境設定
├── ⚙️ application-prod.yaml        # 本番環境設定
├── 📄 database-setup-h2.sql        # H2用初期データ
├── 📁 templates/                   # Quteテンプレート
│   └── login.html                  # ログインページ
└── 📁 META-INF/
    ├── 📁 resources/               # 静的リソース
    │   ├── 📁 css/
    │   │   └── auth.css            # 認証画面スタイル
    │   └── 📁 js/
    │       └── auth.js             # 認証ロジック
    └── 📁 resources/               # JWT鍵ファイル
        ├── privateKey.pem
        └── publicKey.pem
```

### docker/
```
docker/
├── Dockerfile.jvm                 # JVMモード用
├── Dockerfile.native              # ネイティブモード用
├── Dockerfile.native-micro        # ネイティブ軽量版
└── Dockerfile.legacy-jar          # レガシーJAR用
```

## 🗄️ データベース関連

### ddl/ ディレクトリ
```
ddl/
├── database-setup.sql             # PostgreSQL用DDL
└── database-setup-h2.sql          # H2用DDL
```

## 🐳 Docker構成

### docker-compose.yml (本番環境)
- **postgres**: PostgreSQLデータベース
- **quarkus-app**: Quarkusアプリケーション
- **pgadmin**: データベース管理ツール

### docker-compose.dev.yml (開発環境)
- **postgres-dev**: 開発用PostgreSQL (ポート5433)
- **redis-dev**: 将来の拡張用Redis
- **pgadmin-dev**: 開発用pgAdmin (ポート5051)

## 🔧 設定ファイル

### Maven設定
- **pom.xml**: 依存関係、プロファイル設定
- **mvnw/mvnw.cmd**: Mavenラッパー（Java環境不要）

### Quarkus設定
- **application.yaml**: 共通設定
- **application-dev.yaml**: 開発環境（H2データベース）
- **application-prod.yaml**: 本番環境（PostgreSQL）

### Docker設定
- **.dockerignore**: Docker除外ファイル
- **Dockerfile.xxx**: 各種ビルドモード用

## 📋 開発フロー

### 1. 開発環境セットアップ
```bash
# データベース起動
make dev-db

# アプリケーション起動
make dev
```

### 2. 本番環境デプロイ
```bash
# ビルド
make build

# Dockerイメージ作成
make docker-build

# 本番環境起動
make docker-run
```

### 3. テスト実行
```bash
# 単体テスト
make test

# 統合テスト
make test-integration
```

## 🎯 重要なポイント

### ✅ 正しい構造
- Docker関連ファイルは標準的な場所に配置
- Quarkus推奨のディレクトリ構造に準拠
- 環境別設定ファイルで適切に分離

### 🔒 セキュリティ
- JWT鍵ファイルは開発用のみ
- 本番環境では環境変数で管理
- データベース認証情報の適切な管理

### 🚀 拡張性
- マイクロサービス対応の構造
- Docker/Kubernetes対応
- 監視・ログ機能の組み込み
