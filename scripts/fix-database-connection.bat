@echo off
echo 🔧 Исправление подключения к базе данных...

REM Проверяем переменные окружения
if "%POSTGRES_USER%"=="" (
    echo ❌ Не установлена переменная окружения POSTGRES_USER
    echo Установите её в .env файле или экспортируйте:
    echo set POSTGRES_USER=your_user
    exit /b 1
)

if "%POSTGRES_PASSWORD%"=="" (
    echo ❌ Не установлена переменная окружения POSTGRES_PASSWORD
    echo Установите её в .env файле или экспортируйте:
    echo set POSTGRES_PASSWORD=your_password
    exit /b 1
)

echo ✅ Переменные окружения установлены

REM Перезапускаем приложение
echo 🔄 Перезапуск приложения...
docker stop telegram-app
docker rm telegram-app

REM Перезапускаем через GitHub Actions или вручную
echo ✅ Приложение остановлено
echo 📊 Для перезапуска сделайте push в main или запустите вручную:
echo    docker run -d --name telegram-app --restart unless-stopped -p 8080:8080 --add-host=host.docker.internal:host-gateway -e POSTGRES_URL="%POSTGRES_URL%" -e POSTGRES_USER="%POSTGRES_USER%" -e POSTGRES_PASSWORD="%POSTGRES_PASSWORD%" -e SPRING_PROFILES_ACTIVE=prod -v /opt/telegram-app/logs:/app/logs ghcr.io/bjcreslin/naidizakupku-telegram:latest
