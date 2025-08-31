#!/bin/bash

echo "🔧 Исправление проблем деплоя..."

# Останавливаем контейнер если запущен
echo "🛑 Останавливаем старый контейнер..."
docker stop telegram-app || true
docker rm telegram-app || true

# Удаляем старый образ
echo "🗑️ Удаляем старый образ..."
docker rmi ghcr.io/bjcreslin/naidizakupku-telegram:latest || true

# Авторизуемся в GitHub Container Registry
echo "🔐 Авторизация в GitHub Container Registry..."
echo "$GHCR_TOKEN" | docker login -u 'BJCreslin' --password-stdin ghcr.io

# Скачиваем новый образ
echo "📥 Скачиваем новый образ..."
docker pull ghcr.io/bjcreslin/naidizakupku-telegram:latest

# Проверяем переменные окружения
echo "🔍 Проверяем переменные окружения..."
echo "POSTGRES_URL: $POSTGRES_URL"
echo "POSTGRES_USER: $POSTGRES_USER"
echo "POSTGRES_PASSWORD: [HIDDEN]"

# Запускаем новый контейнер с исправленными переменными
echo "🚀 Запускаем новый контейнер..."
docker run -d \
  --name telegram-app \
  --restart unless-stopped \
  -p 8080:8080 \
  -e POSTGRES_URL="$POSTGRES_URL" \
  -e POSTGRES_USER="$POSTGRES_USER" \
  -e POSTGRES_PASSWORD="$POSTGRES_PASSWORD" \
  -e SPRING_PROFILES_ACTIVE=prod \
  -v /opt/telegram-app/logs:/app/logs \
  ghcr.io/bjcreslin/naidizakupku-telegram:latest

# Ждем запуска приложения
echo "⏳ Ждем запуска приложения..."
sleep 30

# Проверяем статус контейнера
echo "📊 Статус контейнера:"
docker ps -a | grep telegram-app || echo "Контейнер не найден"

# Проверяем логи контейнера
echo "📋 Логи контейнера:"
docker logs telegram-app --tail 50 || echo "Логи недоступны"

# Проверяем здоровье приложения
echo "🏥 Проверяем здоровье приложения..."
for i in {1..5}; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ Приложение здорово!"
        break
    else
        echo "❌ Попытка проверки здоровья $i не удалась, ждем..."
        sleep 10
    fi
    
    if [ $i -eq 5 ]; then
        echo "❌ Приложение не запустилось корректно!"
        echo "📋 Последние логи:"
        docker logs telegram-app --tail 100
        exit 1
    fi
done

echo "🎉 Деплой исправлен успешно!"
