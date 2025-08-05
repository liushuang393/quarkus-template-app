#!/bin/bash

# =====================================================
# Quarkusテンプレートプロジェクト作成スクリプト (Unix/Linux版)
# =====================================================
# 作成日: 2025-08-04
# 説明: Quarkus認証・権限管理システムのテンプレートを新しいプロジェクトとして複製
# 使用方法: ./create-project.sh [target_path]
# =====================================================

set -e  # エラー時に終了

echo ""
echo "====================================================="
echo "Quarkus Template Project Creator (Unix/Linux)"
echo "====================================================="
echo ""

# デフォルトのターゲットパス設定
DEFAULT_TARGET="$HOME/workspace/mypj"

# 引数チェック
if [ $# -eq 0 ]; then
    TARGET_PATH="$DEFAULT_TARGET"
    echo "引数が指定されていません。デフォルトパスを使用します。"
    echo "デフォルトパス: $TARGET_PATH"
    echo ""
    read -r -p "このパスでよろしいですか？ (Y/N または新しいパスを入力): " USER_INPUT

    if [ "$USER_INPUT" = "N" ] || [ "$USER_INPUT" = "n" ]; then
        echo "処理を中止しました。"
        exit 0
    fi

    if [ "$USER_INPUT" != "Y" ] && [ "$USER_INPUT" != "y" ] && [ -n "$USER_INPUT" ]; then
        TARGET_PATH="$USER_INPUT"
    fi
else
    TARGET_PATH="$1"
fi

echo "ターゲットパス: $TARGET_PATH"
echo ""

# 新しいプロジェクト名を抽出（パスの最後の部分）
NEW_PROJECT_NAME=$(basename "$TARGET_PATH")
echo "新しいプロジェクト名: $NEW_PROJECT_NAME"
echo ""

# ターゲットディレクトリが既に存在するかチェック
if [ -d "$TARGET_PATH" ]; then
    echo "エラー: ターゲットディレクトリが既に存在します: $TARGET_PATH"
    echo "既存のディレクトリを削除するか、別のパスを指定してください。"
    exit 1
fi

# 親ディレクトリを作成
PARENT_DIR=$(dirname "$TARGET_PATH")
if [ ! -d "$PARENT_DIR" ]; then
    echo "親ディレクトリを作成中: $PARENT_DIR"
    mkdir -p "$PARENT_DIR"
    if ! git clone https://github.com/liushuang393/quarkus-template-app.git "$TARGET_PATH"; then
        echo "エラー: 親ディレクトリの作成に失敗しました。"
        exit 1
    fi
fi

echo "ファイルをコピー中..."
echo ""

# 現在のディレクトリ（テンプレートプロジェクト）を取得
SOURCE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ターゲットディレクトリを作成
mkdir -p "$TARGET_PATH"
if ! cd "$TARGET_PATH"; then
    echo "エラー: ターゲットディレクトリの作成に失敗しました。"
    exit 1
fi

# ファイルとディレクトリをコピー（スクリプトファイルを除く）
echo "- ソースファイルをコピー中..."
if [ -d "$SOURCE_DIR/src" ]; then
    cp -r "$SOURCE_DIR/src" "$TARGET_PATH/"
fi

echo "- 設定ファイルをコピー中..."
[ -f "$SOURCE_DIR/pom.xml" ] && cp "$SOURCE_DIR/pom.xml" "$TARGET_PATH/"
[ -f "$SOURCE_DIR/README.md" ] && cp "$SOURCE_DIR/README.md" "$TARGET_PATH/"
[ -f "$SOURCE_DIR/DATABASE_SETUP.md" ] && cp "$SOURCE_DIR/DATABASE_SETUP.md" "$TARGET_PATH/"
[ -f "$SOURCE_DIR/database-setup.sql" ] && cp "$SOURCE_DIR/database-setup.sql" "$TARGET_PATH/"
[ -f "$SOURCE_DIR/database-setup-h2.sql" ] && cp "$SOURCE_DIR/database-setup-h2.sql" "$TARGET_PATH/"

echo "- DDLディレクトリをコピー中..."
if [ -d "$SOURCE_DIR/ddl" ]; then
    cp -r "$SOURCE_DIR/ddl" "$TARGET_PATH/"
fi

echo "- Mavenラッパーをコピー中..."
[ -f "$SOURCE_DIR/mvnw" ] && cp "$SOURCE_DIR/mvnw" "$TARGET_PATH/"
[ -f "$SOURCE_DIR/mvnw.cmd" ] && cp "$SOURCE_DIR/mvnw.cmd" "$TARGET_PATH/"
if [ -d "$SOURCE_DIR/.mvn" ]; then
    cp -r "$SOURCE_DIR/.mvn" "$TARGET_PATH/"
fi

echo "- その他のファイルをコピー中..."
[ -f "$SOURCE_DIR/.gitignore" ] && cp "$SOURCE_DIR/.gitignore" "$TARGET_PATH/"

# 実行権限を設定
if [ -f "$TARGET_PATH/mvnw" ]; then
    chmod +x "$TARGET_PATH/mvnw"
fi

echo ""
echo "ファイルの内容を更新中..."

# pom.xmlのartifactIdを更新
echo "- pom.xmlを更新中..."
if [ -f "$TARGET_PATH/pom.xml" ]; then
    if command -v sed >/dev/null 2>&1; then
        sed -i.bak "s/quarkus-template-app/$NEW_PROJECT_NAME/g" "$TARGET_PATH/pom.xml"
        rm -f "$TARGET_PATH/pom.xml.bak"
    else
        echo "警告: sedコマンドが見つかりません。pom.xmlを手動で修正してください。"
    fi
fi

# README.mdのプロジェクト名を更新
echo "- README.mdを更新中..."
if [ -f "$TARGET_PATH/README.md" ]; then
    if command -v sed >/dev/null 2>&1; then
        sed -i.bak "s/quarkus-auth-template/$NEW_PROJECT_NAME/g" "$TARGET_PATH/README.md"
        rm -f "$TARGET_PATH/README.md.bak"
    else
        echo "警告: sedコマンドが見つかりません。README.mdを手動で修正してください。"
    fi
fi

echo ""
echo "====================================================="
echo "プロジェクトの作成が完了しました！"
echo "====================================================="
echo ""
echo "新しいプロジェクト: $NEW_PROJECT_NAME"
echo "場所: $TARGET_PATH"
echo ""
echo "次のステップ:"
echo "1. cd \"$TARGET_PATH\""
echo "2. ./mvnw quarkus:dev -Pdev"
echo ""
echo "Eclipse導入手順:"
echo "1. Eclipse を起動"
echo "2. File > Import > Existing Maven Projects"
echo "3. Root Directory: $TARGET_PATH"
echo "4. プロジェクトを選択してFinish"
echo ""
echo "初期ユーザー:"
echo "- admin/AdminPass123 (ADMIN)"
echo "- user/UserPass123 (USER)"
echo "- demouser/DemoPass123 (USER)"
echo ""

# 成功メッセージ
echo "🎉 テンプレートプロジェクトの複製が正常に完了しました！"
echo ""
