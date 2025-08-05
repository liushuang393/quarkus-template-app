# 開発者ガイド

このディレクトリには、新規開発者向けの各層実装ガイドが含まれています。

## 📚 ドキュメント一覧

### 🎨 フロントエンド

- [**フロントエンド開発ガイド**](frontend-guide.md) - HTML、CSS、jQuery、Bootstrap 5を使用したUI開発
- [**コンポーネントガイド**](component-guide.md) - 再利用可能なUIコンポーネントの作成方法

### 🔧 バックエンド

- [**コントローラー層ガイド**](controller-guide.md) - REST APIエンドポイントの実装
- [**サービス層ガイド**](service-guide.md) - ビジネスロジックの実装
- [**データベース層ガイド**](database-guide.md) - MyBatisを使用したデータアクセス層の実装

### 🗄️ データベース

- [**SQLガイド**](sql-guide.md) - データベース設計とSQL実装のベストプラクティス

### 🔒 セキュリティ

- [**セキュリティガイド**](security-guide.md) - JWT認証とロールベースアクセス制御の実装

### 🌐 国際化

- [**国際化ガイド**](i18n-guide.md) - 多言語対応の実装方法

### 🧪 テスト

- [**テストガイド**](testing-guide.md) - 単体テストと統合テストの実装

## 🚀 クイックスタート

1. **環境セットアップ**: [環境構築ガイド](../README.md#環境構築)を参照
2. **アーキテクチャ理解**: [アーキテクチャ概要](../PROJECT_STRUCTURE.md)を確認
3. **開発フロー**: 各層のガイドを順番に読んで実装方法を学習

## 📋 開発規約

### コーディング規約

- **Java**: Google Java Style Guideに準拠
- **JavaScript**: ESLint推奨設定を使用
- **SQL**: 大文字小文字の統一、適切なインデント

### 命名規約

- **クラス名**: PascalCase (例: `UserService`)
- **メソッド名**: camelCase (例: `getUserById`)
- **定数**: UPPER_SNAKE_CASE (例: `MAX_RETRY_COUNT`)
- **データベース**: snake_case (例: `user_id`)

### ファイル構成

```
src/main/java/com/example/
├── controller/     # REST APIコントローラー
├── service/        # ビジネスロジック
├── mapper/         # MyBatisマッパー
├── model/          # データモデル
├── dto/            # データ転送オブジェクト
├── config/         # 設定クラス
└── exception/      # 例外クラス
```

## 🔄 開発ワークフロー

1. **機能設計**: 要件を分析し、API設計を行う
2. **データベース設計**: テーブル設計とSQL作成
3. **バックエンド実装**: モデル → マッパー → サービス → コントローラーの順で実装
4. **フロントエンド実装**: HTML → CSS → JavaScript の順で実装
5. **テスト**: 単体テスト → 統合テスト → E2Eテストの順で実装
6. **ドキュメント更新**: APIドキュメントと開発者ガイドを更新

## 🛠️ 開発ツール

### 推奨IDE

- **IntelliJ IDEA** (推奨)
- **Eclipse**
- **Visual Studio Code**

### 必須プラグイン

- **Lombok**: ボイラープレートコード削減
- **MyBatis**: SQLマッピング支援
- **Quarkus**: 開発効率向上

### デバッグツール

- **Quarkus Dev UI**: http://localhost:8080/q/dev/
- **H2 Console**: http://localhost:8080/h2-console (開発環境)
- **Swagger UI**: http://localhost:8080/q/swagger-ui

## 📞 サポート

質問や問題がある場合は、以下のリソースを活用してください：

- **プロジェクトWiki**: 詳細な技術仕様
- **コードレビュー**: Pull Requestでのフィードバック
- **チームミーティング**: 週次の技術共有会

---

**Happy Coding! 🎉**
