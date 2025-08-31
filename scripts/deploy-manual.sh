#!/bin/bash

# Скрипт для ручного деплоя приложения на сервер
# Используется когда GitHub Actions недоступен

set -e

echo "🚀 Ручной деплой приложения..."

# Проверяем переменные окружения
if [ -z "$POSTGRES_URL" ] || [ -z "$POSTGRES_USER" ] || [ -z "$POSTGRES_PASSWORD" ]; then
    echo "❌ Не установлены переменные окружения для базы данных"
    echo "Установите: POSTGRES_URL, POSTGRES_USER, POSTGRES_PASSWORD"
    exit 1
fi

if [ -z "$TELEGRAM_BOT_TOKEN" ]; then
    echo "❌ Не установлен TELEGRAM_BOT_TOKEN"
    exit 1
fi

# Останавливаем старый контейнер
echo "🛑 Останавливаем старый контейнер..."
docker stop telegram-app || true
docker rm telegram-app || true

# Удаляем старый образ
echo "🗑️ Удаляем старый образ..."
docker rmi ghcr.io/bjcreslin/naidizakupku-telegram:latest || true

# Авторизуемся в GitHub Container Registry
echo "🔐 Авторизация в GitHub Container Registry..."
echo "$GHCR_TOKEN" | docker login -u "$GITHUB_USER" --password-stdin ghcr.io

# Скачиваем новый образ
echo "📥 Скачиваем новый образ..."
docker pull ghcr.io/bjcreslin/naidizakupku-telegram:latest

# Создаем директорию для логов
echo "📁 Создаем директорию для логов..."
sudo mkdir -p /opt/telegram-app/logs
sudo chown $USER:$USER /opt/telegram-app/logs

# Запускаем новый контейнер
echo "🚀 Запускаем новый контейнер..."
docker run -d \
  --name telegram-app \
  --restart unless-stopped \
  -p 8080:8080 \
  --add-host=host.docker.internal:host-gateway \
  -e POSTGRES_URL="$POSTGRES_URL" \
  -e POSTGRES_USER="$POSTGRES_USER" \
  -e POSTGRES_PASSWORD="$POSTGRES_PASSWORD" \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e TELEGRAM_BOT_TOKEN="$TELEGRAM_BOT_TOKEN" \
  -e TELEGRAM_BOT_NAME="$TELEGRAM_BOT_NAME" \
  -e TELEGRAM_BOT_USERNAME="$TELEGRAM_BOT_USERNAME" \
  -v /opt/telegram-app/logs:/app/logs \
  ghcr.io/bjcreslin/naidizakupku-telegram:latest

# Ждем запуска приложения
echo "⏳ Ждем запуска приложения..."
sleep 30

# Проверяем статус контейнера
echo "📊 Статус контейнера:"
docker ps -a | grep telegram-app || echo "Контейнер не найден"

# Проверяем логи
echo "📋 Логи контейнера:"
docker logs telegram-app || echo "Логи недоступны"

# Проверяем здоровье приложения
echo "🏥 Проверяем здоровье приложения..."
for i in {1..10}; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ Приложение запущено и работает!"
        break
    else
        echo "⏳ Попытка $i/10, ждем..."
        sleep 10
    fi
    
    if [ $i -eq 10 ]; then
        echo "❌ Приложение не запустилось!"
        echo "📋 Последние логи:"
        docker logs --tail 50 telegram-app
        exit 1
    fi
done

echo "🎉 Деплой завершен успешно!"
