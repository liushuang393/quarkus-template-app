# コード品質チェック設定

## 概要

このプロジェクトでは、企業レベルのコード品質を保証するため、包括的なpre-commitフックを設定しています。

## チェック対象ファイル

### ✅ **チェック対象**

- `src/main/java/com/example/**/*.java` - メインのJavaソースコード
- `src/test/java/com/example/**/*.java` - テストコード
- `scripts/*.sh` - プロジェクト固有のスクリプト
- `pom.xml` - Maven設定ファイル
- `*.yaml`, `*.yml` - 設定ファイル

### ❌ **チェック除外対象**

#### 🔧 **ビルドと依存関係**

- `target/` - Mavenビルド出力
- `.mvn/` - Maven wrapper
- `node_modules/` - Node.js依存関係
- `build/`, `dist/` - その他のビルド出力
- `vendor/` - 外部ベンダーコード

#### 💻 **IDE・エディタファイル**

- `.idea/`, `*.iml`, `*.ipr`, `*.iws` - IntelliJ IDEA
- `.vscode/` - Visual Studio Code
- `.settings/`, `.project`, `.classpath` - Eclipse
- `*.swp`, `*.swo` - Vim一時ファイル

#### 🗂️ **バージョン管理**

- `.git/` - Gitディレクトリ
- `.svn/`, `.hg/` - その他のVCS

#### 🤖 **生成されたコード**

- `MavenWrapperDownloader.java` - Maven wrapper
- `**/generated/**` - 自動生成コード
- `**/generated-sources/**` - Maven生成ソース
- `**/generated-test-sources/**` - Maven生成テストソース

#### 📦 **バイナリファイル**

- `*.jar`, `*.war`, `*.ear` - Javaアーカイブ
- `*.class` - コンパイル済みJavaクラス
- `*.log`, `*.cache`, `*.lock` - ログ・キャッシュファイル
- `*.tmp`, `*.temp` - 一時ファイル

#### 🖼️ **メディアファイル**

- `*.png`, `*.jpg`, `*.jpeg`, `*.gif`, `*.svg`, `*.ico` - 画像
- `*.pdf`, `*.doc`, `*.docx` - ドキュメント

#### 🔐 **証明書・キーファイル**

- `*.crt`, `*.key`, `*.pem`, `*.p12`, `*.jks` - セキュリティファイル

#### 🖥️ **OS固有ファイル**

- `.DS_Store` - macOS
- `Thumbs.db` - Windows

## 実装されているチェック

### 🔍 **セキュリティチェック**

- SQLインジェクション検出
- ハードコードされた認証情報検出
- 安全でない乱数生成検出
- 安全でないデシリアライゼーション検出
- パストラバーサル脆弱性検出
- 弱い暗号化アルゴリズム検出
- 安全でないHTTP使用検出

### 🏗️ **Quarkus Native互換性チェック**

- リフレクション使用検出
- 動的プロキシ使用検出
- JNI使用検出
- シリアライゼーション使用検出

### 📋 **依存関係チェック**

- Maven設定検証
- 依存関係の脆弱性スキャン
- 古い依存関係の検出

### 🎨 **コードフォーマット**

- Google Java Formatによる統一フォーマット
- 末尾空白の削除
- ファイル末尾改行の統一
- YAML構文チェック（基本的な構文エラー検出）

**注意**: Node.js依存関係を避けるため、prettierは使用していません。

### 📝 **ライセンスヘッダー**

- 全Javaファイルへのライセンスヘッダー挿入

## 使用方法

### 手動実行

```bash
# 全チェック実行
pre-commit run --all-files

# 特定のチェックのみ実行
pre-commit run --all-files security-check
pre-commit run --all-files quarkus-native-compatibility
```

### 自動実行

- コミット時に自動実行
- 問題が検出された場合、コミットが中止される
- 自動修正可能な問題は自動的に修正される

## カスタマイズ

### 除外ファイルの追加

`.pre-commit-config.yaml`の`exclude`パターンを編集：

```yaml
exclude: ^(\.mvn/|target/|your-custom-exclude-pattern)
```

### チェックの無効化

特定のチェックを無効にする場合：

```yaml
# チェックをコメントアウト
# - id: security-check
```

## トラブルシューティング

### Windows環境での問題

- Bashスクリプトが実行できない場合、Git Bashまたは WSL を使用
- パスの区切り文字に注意

### パフォーマンス問題

- 大量のファイルがある場合、除外パターンを調整
- 必要に応じて特定のチェックを無効化

## 参考資料

- [Pre-commit公式ドキュメント](https://pre-commit.com/)
- [Google Java Format](https://github.com/google/google-java-format)
- [Quarkus Native Build Guide](https://quarkus.io/guides/building-native-image)
