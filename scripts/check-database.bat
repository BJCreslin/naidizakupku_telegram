@echo off
echo 🔍 Проверка подключения к базе данных...

REM Проверяем переменные окружения
if "%POSTGRES_URL%"=="" (
    echo ❌ POSTGRES_URL не установлен
    exit /b 1
)

if "%POSTGRES_USER%"=="" (
    echo ❌ POSTGRES_USER не установлен
    exit /b 1
)

if "%POSTGRES_PASSWORD%"=="" (
    echo ❌ POSTGRES_PASSWORD не установлен
    exit /b 1
)

echo ✅ Переменные окружения установлены

REM Извлекаем хост и порт из URL
for /f "tokens=3 delims=/" %%a in ("%POSTGRES_URL%") do set HOST_PORT=%%a
for /f "tokens=1 delims=:" %%a in ("%HOST_PORT%") do set HOST=%%a
for /f "tokens=2 delims=:" %%a in ("%HOST_PORT%") do set PORT=%%a

echo 🌐 Хост: %HOST%
echo 🔌 Порт: %PORT%

REM Проверяем доступность порта
powershell -Command "Test-NetConnection -ComputerName %HOST% -Port %PORT% -InformationLevel Quiet" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Порт %PORT% доступен на %HOST%
) else (
    echo ❌ Порт %PORT% недоступен на %HOST%
    exit /b 1
)

REM Пробуем подключиться к БД
echo 🔐 Подключение к базе данных...
set PGPASSWORD=%POSTGRES_PASSWORD%
psql -h %HOST% -p %PORT% -U %POSTGRES_USER% -d telegram_db -c "SELECT version();" >nul 2>&1

if %errorlevel% equ 0 (
    echo ✅ Подключение к БД успешно
    
    REM Проверяем таблицы
    echo 📋 Проверка таблиц...
    psql -h %HOST% -p %PORT% -U %POSTGRES_USER% -d telegram_db -c "\dt" 2>nul
    
    REM Проверяем таблицу Liquibase
    echo 🔄 Проверка таблицы Liquibase...
    psql -h %HOST% -p %PORT% -U %POSTGRES_USER% -d telegram_db -c "SELECT COUNT(*) FROM databasechangelog;" 2>nul
    
) else (
    echo ❌ Не удалось подключиться к БД
    exit /b 1
)
