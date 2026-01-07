@echo off
REM Скрипт для создания первого администратора (Windows)
REM Использование: create-first-admin.bat <username> <password> [email]

setlocal enabledelayedexpansion

REM Проверка аргументов
if "%~1"=="" (
    echo Ошибка: Недостаточно аргументов
    echo Использование: %0 ^<username^> ^<password^> [email]
    echo Пример: %0 admin mypassword123 admin@example.com
    exit /b 1
)

if "%~2"=="" (
    echo Ошибка: Недостаточно аргументов
    echo Использование: %0 ^<username^> ^<password^> [email]
    exit /b 1
)

set USERNAME=%~1
set PASSWORD=%~2
set EMAIL=%~3

REM URL API (можно переопределить через переменную окружения)
if "%API_URL%"=="" set API_URL=http://localhost:8080

echo Создание первого администратора...
echo Username: %USERNAME%
if not "%EMAIL%"=="" echo Email: %EMAIL%
echo API URL: %API_URL%
echo.

REM Проверяем, есть ли уже администраторы
echo Проверка наличия администраторов...
for /f "tokens=*" %%i in ('curl -s "%API_URL%/api/admin/auth/has-admins"') do set HAS_ADMINS_RESPONSE=%%i

echo %HAS_ADMINS_RESPONSE% | findstr /C:"\"hasAdmins\":true" >nul
if %errorlevel% equ 0 (
    echo Ошибка: В системе уже есть администраторы
    echo Используйте обычную регистрацию или обратитесь к существующему администратору
    exit /b 1
)

REM Создаем JSON payload
if not "%EMAIL%"=="" (
    set JSON_PAYLOAD={"username": "%USERNAME%", "password": "%PASSWORD%", "email": "%EMAIL%"}
) else (
    set JSON_PAYLOAD={"username": "%USERNAME%", "password": "%PASSWORD%"}
)

REM Отправляем запрос
echo Отправка запроса на создание администратора...
curl -X POST "%API_URL%/api/admin/auth/register-first-admin" ^
    -H "Content-Type: application/json" ^
    -d "%JSON_PAYLOAD%"

if %errorlevel% equ 0 (
    echo.
    echo Администратор успешно создан!
    echo.
    echo Теперь вы можете войти в админку используя:
    echo   Username: %USERNAME%
    echo   Password: %PASSWORD%
    echo.
    echo Админка доступна по адресу: %API_URL%/admin
) else (
    echo.
    echo Ошибка при создании администратора
    exit /b 1
)

