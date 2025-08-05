# Quarkus 認証・権限管理システム

[![Quarkus](https://img.shields.io/badge/Quarkus-3.25.0-blue?style=flat-square&logo=quarkus)](https://quarkus.io/)
[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

> 🚀 **企業レベルの認証・権限管理システムテンプレート**
> JWT認証、ロールベースアクセス制御、国際化対応、MyBatis統合

## ⚠️ **重要なセキュリティ警告**

> **🔴 本番環境で使用する前に必ず以下を変更してください！**
>
> ### 📋 **必須変更項目**
>
> 1. **デフォルトユーザーの変更**：
>    - `ddl/database-setup.sql` 内のデフォルトユーザー（admin, sales, user等）
>    - すべてのデフォルトパスワードを強力なものに変更
>    - デフォルトメールアドレスを実際のものに変更
>
> 2. **JWT設定の変更**：
>    - `src/main/resources/application-prod.yaml` の JWT秘密鍵
>    - JWT有効期限の調整
>
> 3. **データベース認証情報**：
>    - データベースユーザー名・パスワード
>    - 接続文字列の変更
>
> **デフォルト認証情報（開発・テスト用のみ）**：
> - 管理者: `admin` / `AdminPass123`
> - 営業: `sales` / `SalesPass123`
> - 一般: `user` / `UserPass123`
>
> **これらの認証情報は本番環境では絶対に使用しないでください！**

## ✨ 主要機能

- 🔐 **JWT認証システム** - セキュアなトークンベース認証
- 👥 **ロールベースアクセス制御** - ADMIN/SALES/USER権限管理
- 🌐 **国際化対応** - 日本語/英語/中国語サポート
- 📊 **ダッシュボード** - リアルタイム統計とアクティビティ監視
- 🗄️ **MyBatis統合** - 効率的なデータアクセス層
- 📱 **レスポンシブUI** - Bootstrap 5 + jQuery

## 🚀 クイックスタート

### 前提条件

- Java 21+
- Maven 3.8+

### 起動手順

```bash
# 1. リポジトリクローン
git clone https://github.com/liushuang393/quarkus-template-app.git
cd quarkus-template-app

# 2. 開発モードで起動
./mvnw quarkus:dev

# 3. ブラウザでアクセス
open http://localhost:8080
```

### 初期ユーザー

| ユーザー名 | パスワード | ロール | 説明           |
| ---------- | ---------- | ------ | -------------- |
| `admin`    | `password` | ADMIN  | 全機能アクセス |
| `user1`    | `password` | USER   | 基本機能のみ   |
| `sales1`   | `password` | SALES  | 営業機能       |

## 🏗️ アーキテクチャ

```mermaid
graph TB
    A[Frontend<br/>Qute + Bootstrap + jQuery] --> B[Quarkus REST API]
    B --> C[MyBatis]
    C --> D[(PostgreSQL/H2)]
    B --> E[JWT Service]
    B --> F[Message Service<br/>i18n]
```

### 技術スタック

| 層           | 技術                      | バージョン    |
| ------------ | ------------------------- | ------------- |
| **Backend**  | Quarkus                   | 3.25.0        |
| **Frontend** | Qute + Bootstrap + jQuery | 5.3.0 + 3.7.1 |
| **Database** | PostgreSQL / H2           | 15 / 2.x      |
| **ORM**      | MyBatis                   | 3.5.x         |
| **Security** | SmallRye JWT              | -             |
| **Build**    | Maven                     | 3.8+          |

## 🆚 Quarkus vs Spring Boot の優位性

### ⚡ パフォーマンス優位性

| 項目                     | Quarkus        | Spring Boot       |
| ------------------------ | -------------- | ----------------- |
| **起動時間**             | 0.1秒以下      | 3-10秒            |
| **メモリ使用量**         | 30-50MB        | 200-500MB         |
| **ネイティブコンパイル** | ✅ GraalVM対応 | ❌ 限定的サポート |
| **コンテナサイズ**       | 50-100MB       | 200-500MB         |
| **コールドスタート**     | 瞬時           | 数秒              |

### 🛡️ 開発品質保証

このプロジェクトでは、コード品質とネイティブビルド互換性を保つための包括的なチェック機能を提供：

- **セキュリティパターン検査** - SQLインジェクション、ハードコード認証情報等の検出
- **ネイティブ互換性チェック** - GraalVM 非互換パターンの事前検出
- **依存関係脆弱性スキャン** - セキュリティリスクの早期発見
- **コードフォーマット** - Google Java Format による一貫したスタイル維持

### 📊 実測パフォーマンス比較

**テスト環境**: Intel i7-12700K, 32GB RAM, SSD

| 測定項目                  | Quarkus (JVM) | Quarkus (Native) | Spring Boot |
| ------------------------- | ------------- | ---------------- | ----------- |
| **起動時間**              | 1.2秒         | 0.08秒           | 8.5秒       |
| **初回リクエスト**        | 50ms          | 15ms             | 200ms       |
| **メモリ使用量**          | 85MB          | 35MB             | 320MB       |
| **Docker イメージサイズ** | 180MB         | 65MB             | 450MB       |
| **ビルド時間**            | 15秒          | 2分30秒          | 25秒        |

**結論**: ネイティブビルドにより、起動時間とメモリ使用量で圧倒的な優位性を実現

## 📚 ドキュメント

| ドキュメント                                    | 説明                           |
| ----------------------------------------------- | ------------------------------ |
| [📖 プロジェクト構造](PROJECT_STRUCTURE.md)     | ディレクトリ構成とファイル説明 |
| [🗄️ データベース設定](DATABASE_SETUP.md)        | DB環境構築手順                 |
| [🔧 開発者ガイド](docs/README.md)               | 各層の実装ガイド               |
| [🌐 国際化ガイド](docs/i18n-guide.md)           | 多言語対応の実装方法           |
| [🔒 セキュリティガイド](docs/security-guide.md) | JWT認証とアクセス制御          |
| [🧪 テストガイド](docs/testing-guide.md)        | テスト実装方法                 |

## 🛠️ 開発ワークフロー

### 📝 コード開発からGitHub提出まで

#### 1. 開発環境セットアップ

```bash
# プロジェクトクローン
git clone https://github.com/liushuang393/quarkus-template-app.git
cd quarkus-template-app

# Pre-commitフックのインストール（推奨）
pip install pre-commit
pre-commit install
```

#### 2. 開発・テスト

```bash
# 開発モードで起動（ホットリロード有効）
./mvnw quarkus:dev

# JVMモードでビルド（開発環境）
./mvnw clean package -Pdev

# ネイティブビルド（開発環境）
./mvnw clean package -Pdev -Dnative

# 別ターミナルでテスト実行
./mvnw test

# 特定のテストクラスのみ実行
./mvnw test -Dtest=UserServiceTest
```

#### 3. 静的チェック・品質保証

```bash
# 全体的な品質チェック（pre-commitフック）
pre-commit run --all-files

# 特定のフックのみ実行
pre-commit run security-check
pre-commit run quarkus-native-compatibility
pre-commit run maven-dependency-check

# 個別チェック実行
./scripts/check-security-patterns.sh src/main/java/com/example/*.java
./scripts/check-native-compatibility.sh src/main/java/com/example/*.java
./scripts/check-dependencies.sh
./scripts/check-qute-templates.sh src/main/resources/templates/*.html
```

#### 4. ビルド・パッケージング

```bash
# 開発環境用ビルド（H2データベース）
./mvnw clean package -Pdev

# 本番環境用ビルド（PostgreSQL）
./mvnw clean package -Pprod

# ネイティブビルド（開発環境）
./mvnw clean package -Pdev -Dnative

# ネイティブビルド（本番環境）
./mvnw clean package -Pprod -Dnative

# ネイティブビルドテスト
./target/quarkus-template-app-1.0.0-SNAPSHOT-runner
```

#### 5. Docker化

```bash
# JVMモードでDockerイメージ作成
./mvnw package
docker build -f docker/Dockerfile.jvm -t quarkus-auth:latest .

# ネイティブモードでDockerイメージ作成
./mvnw package -Dnative
docker build -f docker/Dockerfile.native -t quarkus-auth:native .

# コンテナ起動
docker run -i --rm -p 8080:8080 quarkus-auth:latest
```

#### 6. GitHub提出

```bash
# 変更をステージング
git add .

# コミット（pre-commitフックが自動実行される）
git commit -m "feat: add new feature"

# リモートにプッシュ
git push origin main

# プルリクエスト作成（GitHub Web UI）
```

### 🔍 品質チェック詳細

#### ネイティブ互換性チェック

```bash
# 特定ファイルのチェック
./scripts/check-native-compatibility.sh src/main/java/com/example/UserService.java

# 複数ファイルのチェック
./scripts/check-native-compatibility.sh src/main/java/com/example/*.java

# 結果例：
# ✅ リフレクション使用なし
# ✅ 動的プロキシ使用なし
# ✅ GraalVM互換
```

#### 依存関係脆弱性チェック

```bash
./scripts/check-dependencies.sh

# 結果例：
# ✅ 既知の脆弱性なし
# ⚠️  更新推奨: jackson-databind 2.15.0 → 2.15.2
```

#### Quteテンプレート検証

```bash
./scripts/check-qute-templates.sh src/main/resources/templates/login.html

# 結果例：
# ✅ 構文エラーなし
# ✅ 変数参照正常
# ✅ ネイティブビルド対応
```

## 🔗 重要なURL

| サービス             | URL                                | 説明               |
| -------------------- | ---------------------------------- | ------------------ |
| **アプリケーション** | http://localhost:8080              | メインアプリ       |
| **ダッシュボード**   | http://localhost:8080/dashboard    | 管理画面           |
| **Swagger UI**       | http://localhost:8080/q/swagger-ui | API ドキュメント   |
| **Dev UI**           | http://localhost:8080/q/dev        | 開発者ツール       |
| **H2 Console**       | http://localhost:8080/h2-console   | DB管理（開発環境） |
| **Health Check**     | http://localhost:8080/q/health     | ヘルスチェック     |

## 🚨 トラブルシューティング

### よくある問題

<details>
<summary>ポート8080が使用中</summary>

```bash
# 別ポートで起動
./mvnw quarkus:dev -Dquarkus.http.port=8081
```

</details>

<details>
<summary>データベース接続エラー</summary>

開発環境（H2）: アプリケーション再起動
本番環境（PostgreSQL）: `application-prod.yaml`の接続設定を確認

</details>

<details>
<summary>JWT署名エラー</summary>

```bash
# 鍵ファイルの存在確認
ls src/main/resources/META-INF/resources/*.pem
```

</details>

## 🤝 貢献

プルリクエストや課題報告を歓迎します！

1. このリポジトリをフォーク
2. フィーチャーブランチを作成 (`git checkout -b feature/amazing-feature`)
3. 変更をコミット (`git commit -m 'Add amazing feature'`)
4. ブランチにプッシュ (`git push origin feature/amazing-feature`)
5. プルリクエストを作成

## 🙏 謝辞

このプロジェクトは以下の素晴らしいオープンソースプロジェクトによって支えられています：

### 🚀 フレームワーク・ライブラリ

- **[Quarkus](https://quarkus.io/)** - 超高速起動とクラウドネイティブJavaフレームワーク
- **[MyBatis](https://mybatis.org/)** - 優れたSQL マッピングフレームワーク
- **[Bootstrap](https://getbootstrap.com/)** - 世界で最も人気のあるCSSフレームワーク
- **[jQuery](https://jquery.com/)** - 高速で軽量なJavaScriptライブラリ

### 🛠️ 開発ツール

- **[Maven](https://maven.apache.org/)** - プロジェクト管理とビルドツール
- **[H2 Database](https://www.h2database.com/)** - 高速インメモリデータベース
- **[PostgreSQL](https://www.postgresql.org/)** - 世界で最も先進的なオープンソースデータベース

### 🔒 セキュリティ

- **[SmallRye JWT](https://smallrye.io/)** - MicroProfile JWT実装
- **[BCrypt](https://github.com/patrickfav/bcrypt)** - 安全なパスワードハッシュ化

これらのプロジェクトの開発者とコントリビューターの皆様に深く感謝いたします。

## 📄 ライセンス

このプロジェクトは [MIT License](LICENSE) の下で公開されています。

## 🆘 サポート

- 📖 [Quarkus公式ドキュメント](https://quarkus.io/guides/)
- 💬 [Quarkus Zulip Chat](https://quarkusio.zulipchat.com/)
- 🐛 [GitHub Issues](https://github.com/liushuang393/quarkus-template-app/issues)

---

<div align="center">

**Made with ❤️ using Quarkus**

[⭐ Star this repo](https://github.com/liushuang393/quarkus-template-app) • [🐛 Report Bug](https://github.com/liushuang393/quarkus-template-app/issues) • [✨ Request Feature](https://github.com/liushuang393/quarkus-template-app/issues)

</div>
