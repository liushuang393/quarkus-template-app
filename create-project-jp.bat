@echo off
setlocal enabledelayedexpansion
chcp 65001
REM =====================================================
REM Quarkusテンプレートプロジェクト作成スクリプト (Windows版)
REM =====================================================
REM 作成日: 2025-08-04
REM 説明: Quarkus認証・権限管理システムのテンプレートを新しいプロジェクトとして複製
REM 使用方法: create-project.bat [target_path]
REM =====================================================

echo.
echo =====================================================
echo Quarkus Template Project Creator (Windows)
echo =====================================================
echo.

REM デフォルトのターゲットパス設定
set "DEFAULT_TARGET=D:\workspace\mypj"

REM 引数チェック
if "%~1"=="" (
    set "TARGET_PATH=%DEFAULT_TARGET%"
    echo 引数が指定されていません。デフォルトパスを使用します。
    echo デフォルトパス: !TARGET_PATH!
    echo.
    set /p "USER_INPUT=このパスでよろしいですか？ (Y/N または新しいパスを入力): "

    if /i "!USER_INPUT!"=="N" (
        echo 処理を中止しました。
        goto :end
    )

    if /i not "!USER_INPUT!"=="Y" (
        if not "!USER_INPUT!"=="" (
            set "TARGET_PATH=!USER_INPUT!"
        )
    )
) else (
    set "TARGET_PATH=%~1"
)

echo ターゲットパス: !TARGET_PATH!
echo.

REM 新しいプロジェクト名を抽出（パスの最後の部分）
for %%i in ("!TARGET_PATH!") do set "NEW_PROJECT_NAME=%%~ni"
echo 新しいプロジェクト名: !NEW_PROJECT_NAME!
echo.

REM ターゲットディレクトリが既に存在するかチェック
if exist "!TARGET_PATH!" (
    echo エラー: ターゲットディレクトリが既に存在します: !TARGET_PATH!
    echo 既存のディレクトリを削除するか、別のパスを指定してください。
    goto :end
)

REM 親ディレクトリを作成
for %%i in ("!TARGET_PATH!") do set "PARENT_DIR=%%~dpi"
if not exist "!PARENT_DIR!" (
    echo 親ディレクトリを作成中: !PARENT_DIR!
    mkdir "!PARENT_DIR!" 2>nul
    if errorlevel 1 (
        echo エラー: 親ディレクトリの作成に失敗しました。
        goto :end
    )
)

echo ファイルをコピー中...
echo.

REM 現在のディレクトリ（テンプレートプロジェクト）を取得
set "SOURCE_DIR=%~dp0"
set "SOURCE_DIR=!SOURCE_DIR:~0,-1!"

REM ターゲットディレクトリを作成
mkdir "!TARGET_PATH!" 2>nul
if errorlevel 1 (
    echo エラー: ターゲットディレクトリの作成に失敗しました。
    goto :end
)

REM ファイルとディレクトリをコピー（スクリプトファイルを除く）
echo - ソースファイルをコピー中...
xcopy "!SOURCE_DIR!\src" "!TARGET_PATH!\src" /E /I /H /Y >nul
if errorlevel 1 (
    echo エラー: srcディレクトリのコピーに失敗しました。
    goto :cleanup
)

echo - 設定ファイルをコピー中...
copy "!SOURCE_DIR!\pom.xml" "!TARGET_PATH!\pom.xml" >nul
copy "!SOURCE_DIR!\README.md" "!TARGET_PATH!\README.md" >nul
copy "!SOURCE_DIR!\DATABASE_SETUP.md" "!TARGET_PATH!\DATABASE_SETUP.md" >nul
copy "!SOURCE_DIR!\database-setup.sql" "!TARGET_PATH!\database-setup.sql" >nul
copy "!SOURCE_DIR!\database-setup-h2.sql" "!TARGET_PATH!\database-setup-h2.sql" >nul

echo - DDLディレクトリをコピー中...
if exist "!SOURCE_DIR!\ddl" (
    xcopy "!SOURCE_DIR!\ddl" "!TARGET_PATH!\ddl" /E /I /H /Y >nul
)

echo - Mavenラッパーをコピー中...
copy "!SOURCE_DIR!\mvnw" "!TARGET_PATH!\mvnw" >nul
copy "!SOURCE_DIR!\mvnw.cmd" "!TARGET_PATH!\mvnw.cmd" >nul
if exist "!SOURCE_DIR!\.mvn" (
    xcopy "!SOURCE_DIR!\.mvn" "!TARGET_PATH!\.mvn" /E /I /H /Y >nul
)

echo - その他のファイルをコピー中...
if exist "!SOURCE_DIR!\.gitignore" copy "!SOURCE_DIR!\.gitignore" "!TARGET_PATH!\.gitignore" >nul

echo.
echo ファイルの内容を更新中...

REM pom.xmlのartifactIdを更新
echo - pom.xmlを更新中...
powershell -Command "(Get-Content '!TARGET_PATH!\pom.xml') -replace 'quarkus-template-app', '!NEW_PROJECT_NAME!' | Set-Content '!TARGET_PATH!\pom.xml'"
if errorlevel 1 (
    echo 警告: pom.xmlの更新に失敗しました。手動で修正してください。
)

REM README.mdのプロジェクト名を更新
echo - README.mdを更新中...
powershell -Command "(Get-Content '!TARGET_PATH!\README.md') -replace 'quarkus-auth-template', '!NEW_PROJECT_NAME!' | Set-Content '!TARGET_PATH!\README.md'"
if errorlevel 1 (
    echo 警告: README.mdの更新に失敗しました。手動で修正してください。
)

echo.
echo =====================================================
echo プロジェクトの作成が完了しました！
echo =====================================================
echo.
echo 新しいプロジェクト: !NEW_PROJECT_NAME!
echo 場所: !TARGET_PATH!
echo.
echo 次のステップ:
echo 1. cd "!TARGET_PATH!"
echo 2. ./mvnw quarkus:dev -Pdev
echo.
echo Eclipse導入手順:
echo 1. Eclipse を起動
echo 2. File ^> Import ^> Existing Maven Projects
echo 3. Root Directory: !TARGET_PATH!
echo 4. プロジェクトを選択してFinish
echo.
echo 初期ユーザー:
echo - admin/AdminPass123 (ADMIN)
echo - user/UserPass123 (USER)
echo - demouser/DemoPass123 (USER)
echo.
goto :end

:cleanup
echo クリーンアップ中...
if exist "!TARGET_PATH!" rmdir /s /q "!TARGET_PATH!"

:end
echo 処理を終了します。
pause
