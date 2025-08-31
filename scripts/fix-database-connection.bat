@echo off
REM Скрипт для исправления проблемы с подключением к базе данных

echo 🔧 Исправление проблемы с подключением к базе данных...

REM Проверяем, что мы в корневой директории проекта
if not exist "build.gradle.kts" (
    echo ❌ Ошибка: Запустите скрипт из корневой директории проекта
    exit /b 1
)

echo 📋 Проверка текущих настроек...

REM Проверяем, запущен ли PostgreSQL
docker ps | findstr postgres >nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ PostgreSQL контейнер запущен
) else (
    echo ⚠️  PostgreSQL контейнер не найден
    echo 🚀 Запускаем PostgreSQL...
    docker-compose up -d postgres
)

REM Проверяем переменные окружения
if exist ".env" (
    echo ✅ Файл .env найден
    
    REM Проверяем POSTGRES_URL
    findstr "POSTGRES_URL" .env >nul
    if %ERRORLEVEL% EQU 0 (
        echo ✅ POSTGRES_URL настроен
        findstr "POSTGRES_URL" .env
    ) else (
        echo ❌ POSTGRES_URL не найден в .env
        echo 📝 Добавьте в .env:
        echo    POSTGRES_URL=jdbc:postgresql://postgres:5432/telegram_db
    )
) else (
    echo ⚠️  Файл .env не найден
    echo 📝 Создайте .env файл из env.example:
    echo    copy env.example .env
)

echo.
echo 🔍 Возможные решения:
echo.
echo 1. Для локальной разработки с Docker Compose:
echo    POSTGRES_URL=jdbc:postgresql://postgres:5432/telegram_db
echo.
echo 2. Для продакшена (PostgreSQL на VPS):
echo    POSTGRES_URL=jdbc:postgresql://5.44.40.79:5432/telegram_db
echo.
echo 3. Для локальной разработки без Docker:
echo    POSTGRES_URL=jdbc:postgresql://localhost:5432/telegram_db
echo.
echo 🚀 Запуск приложения:
echo    # С профилем dev (Docker Compose)
echo    gradlew bootRun --args="--spring.profiles.active=dev"
echo.
echo    # С профилем prod (VPS)
echo    gradlew bootRun --args="--spring.profiles.active=prod"
