@echo off
setlocal enabledelayedexpansion

REM =====================================================
REM Quarkus Template Project Creator (Windows)
REM =====================================================
REM Created: 2025-08-04
REM Description: Copy Quarkus Authentication Template to new project
REM Usage: create-project-en.bat [target_path]
REM =====================================================

echo.
echo =====================================================
echo Quarkus Template Project Creator (Windows)
echo =====================================================
echo.

REM Default target path setting
set "DEFAULT_TARGET=D:\workspace\mypj"

REM Check arguments
if "%~1"=="" (
    set "TARGET_PATH=%DEFAULT_TARGET%"
    echo No arguments specified. Using default path.
    echo Default path: !TARGET_PATH!
    echo.
    set /p "USER_INPUT=Is this path OK? (Y/N or enter new path): "

    if /i "!USER_INPUT!"=="N" (
        echo Process cancelled.
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

echo Target path: !TARGET_PATH!
echo.

REM Extract new project name (last part of path)
for %%i in ("!TARGET_PATH!") do set "NEW_PROJECT_NAME=%%~ni"
echo New project name: !NEW_PROJECT_NAME!
echo.

REM Check if target directory already exists
if exist "!TARGET_PATH!" (
    echo Error: Target directory already exists: !TARGET_PATH!
    echo Please remove existing directory or specify different path.
    goto :end
)

REM Create parent directory
for %%i in ("!TARGET_PATH!") do set "PARENT_DIR=%%~dpi"
if not exist "!PARENT_DIR!" (
    echo Creating parent directory: !PARENT_DIR!
    mkdir "!PARENT_DIR!" 2>nul
    if errorlevel 1 (
        echo Error: Failed to create parent directory.
        goto :end
    )
)

echo Copying files...
echo.

REM Get current directory (template project)
set "SOURCE_DIR=%~dp0"
set "SOURCE_DIR=!SOURCE_DIR:~0,-1!"

REM Create target directory
mkdir "!TARGET_PATH!" 2>nul
if errorlevel 1 (
    echo Error: Failed to create target directory.
    goto :end
)

REM Copy files and directories (excluding script files)
echo - Copying source files...
xcopy "!SOURCE_DIR!\src" "!TARGET_PATH!\src" /E /I /H /Y >nul
if errorlevel 1 (
    echo Error: Failed to copy src directory.
    goto :cleanup
)

echo - Copying configuration files...
copy "!SOURCE_DIR!\pom.xml" "!TARGET_PATH!\pom.xml" >nul
copy "!SOURCE_DIR!\README.md" "!TARGET_PATH!\README.md" >nul
copy "!SOURCE_DIR!\DATABASE_SETUP.md" "!TARGET_PATH!\DATABASE_SETUP.md" >nul
copy "!SOURCE_DIR!\database-setup.sql" "!TARGET_PATH!\database-setup.sql" >nul
copy "!SOURCE_DIR!\database-setup-h2.sql" "!TARGET_PATH!\database-setup-h2.sql" >nul

echo - Copying DDL directory...
if exist "!SOURCE_DIR!\ddl" (
    xcopy "!SOURCE_DIR!\ddl" "!TARGET_PATH!\ddl" /E /I /H /Y >nul
)

echo - Copying Maven wrapper...
copy "!SOURCE_DIR!\mvnw" "!TARGET_PATH!\mvnw" >nul
copy "!SOURCE_DIR!\mvnw.cmd" "!TARGET_PATH!\mvnw.cmd" >nul
if exist "!SOURCE_DIR!\.mvn" (
    xcopy "!SOURCE_DIR!\.mvn" "!TARGET_PATH!\.mvn" /E /I /H /Y >nul
)

echo - Copying other files...
if exist "!SOURCE_DIR!\.gitignore" copy "!SOURCE_DIR!\.gitignore" "!TARGET_PATH!\.gitignore" >nul

echo.
echo Updating file contents...

REM Update artifactId in pom.xml
echo - Updating pom.xml...
powershell -Command "(Get-Content '!TARGET_PATH!\pom.xml') -replace 'quarkus-template-app', '!NEW_PROJECT_NAME!' | Set-Content '!TARGET_PATH!\pom.xml'"
if errorlevel 1 (
    echo Warning: Failed to update pom.xml. Please update manually.
)

REM Update project name in README.md
echo - Updating README.md...
powershell -Command "(Get-Content '!TARGET_PATH!\README.md') -replace 'quarkus-auth-template', '!NEW_PROJECT_NAME!' | Set-Content '!TARGET_PATH!\README.md'"
if errorlevel 1 (
    echo Warning: Failed to update README.md. Please update manually.
)

echo.
echo =====================================================
echo Project creation completed successfully!
echo =====================================================
echo.
echo New project: !NEW_PROJECT_NAME!
echo Location: !TARGET_PATH!
echo.
echo Next steps:
echo 1. cd "!TARGET_PATH!"
echo 2. ./mvnw quarkus:dev -Pdev
echo.
echo Eclipse import steps:
echo 1. Start Eclipse
echo 2. File ^> Import ^> Existing Maven Projects
echo 3. Root Directory: !TARGET_PATH!
echo 4. Select project and Finish
echo.
echo Initial users:
echo - admin/AdminPass123 (ADMIN)
echo - user/UserPass123 (USER)
echo - demouser/DemoPass123 (USER)
echo.
goto :end

:cleanup
echo Cleaning up...
if exist "!TARGET_PATH!" rmdir /s /q "!TARGET_PATH!"

:end
echo Process finished.
pause
