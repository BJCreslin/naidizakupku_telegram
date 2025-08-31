#!/bin/bash

echo "🔧 Исправление подключения к базе данных..."

# Проверяем, что PostgreSQL запущен локально
if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "❌ PostgreSQL не запущен на localhost:5432"
    echo "Запустите PostgreSQL локально:"
    echo "sudo systemctl start postgresql"
    exit 1
fi

echo "✅ PostgreSQL доступен на localhost:5432"

# Проверяем переменные окружения
if [ -z "$POSTGRES_USER" ] || [ -z "$POSTGRES_PASSWORD" ]; then
    echo "❌ Не установлены переменные окружения POSTGRES_USER или POSTGRES_PASSWORD"
    echo "Установите их в .env файле или экспортируйте:"
    echo "export POSTGRES_USER=your_user"
    echo "export POSTGRES_PASSWORD=your_password"
    exit 1
fi

echo "✅ Переменные окружения установлены"

# Перезапускаем приложение
echo "🔄 Перезапуск приложения..."
docker stop telegram-app || true
docker rm telegram-app || true

# Перезапускаем через GitHub Actions или вручную
echo "✅ Приложение остановлено"
echo "📊 Для перезапуска сделайте push в main или запустите вручную:"
echo "   docker run -d --name telegram-app --restart unless-stopped -p 8080:8080 --add-host=host.docker.internal:host-gateway -e POSTGRES_URL=\"$POSTGRES_URL\" -e POSTGRES_USER=\"$POSTGRES_USER\" -e POSTGRES_PASSWORD=\"$POSTGRES_PASSWORD\" -e SPRING_PROFILES_ACTIVE=prod -v /opt/telegram-app/logs:/app/logs ghcr.io/bjcreslin/naidizakupku-telegram:latest"
