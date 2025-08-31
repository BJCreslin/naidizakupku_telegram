#!/bin/bash

# Скрипт для исправления запущенного приложения
# Использование: ./fix-app.sh

set -e

VPS_IP="5.44.40.79"
VPS_USER="root"

echo "🔧 Исправляем запущенное приложение на VPS $VPS_IP..."

# Проверяем подключение к VPS
echo "📡 Проверяем подключение к VPS..."
ssh -o ConnectTimeout=10 $VPS_USER@$VPS_IP "echo 'Подключение успешно'"

# Останавливаем старое приложение
echo "🛑 Останавливаем старое приложение..."
ssh $VPS_USER@$VPS_IP "docker stop telegram-app || true"
ssh $VPS_USER@$VPS_IP "docker rm telegram-app || true"

# Запускаем приложение с правильными переменными окружения
echo "🚀 Запускаем приложение с исправленными переменными..."
ssh $VPS_USER@$VPS_IP "docker run -d \
  --name telegram-app \
  --restart unless-stopped \
  -p 8080:8080 \
  -e POSTGRES_URL=jdbc:postgresql://5.44.40.79:5432/telegram_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=your_password \
  -e KAFKA_BOOTSTRAP_SERVERS=5.44.40.79:9092 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -v /opt/telegram-app/logs:/app/logs \
  ghcr.io/bjcreslin/naidizakupku-telegram:latest"

# Ждем запуска приложения
echo "⏳ Ждем запуска приложения..."
sleep 15

# Проверяем статус приложения
echo "🔍 Проверяем статус приложения..."
ssh $VPS_USER@$VPS_IP "docker ps | grep telegram-app"

# Проверяем логи приложения
echo "📝 Проверяем логи приложения..."
ssh $VPS_USER@$VPS_IP "docker logs telegram-app --tail 20"

# Проверяем здоровье приложения
echo "💚 Проверяем здоровье приложения..."
ssh $VPS_USER@$VPS_IP "curl -s http://localhost:8080/actuator/health || echo 'Приложение еще не готово'"

echo "🎉 Приложение исправлено!"
echo "🌐 Приложение доступно по адресу: http://$VPS_IP:8080"
echo "📝 Логи: ssh $VPS_USER@$VPS_IP 'docker logs telegram-app -f'"
