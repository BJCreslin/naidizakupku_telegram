#!/bin/bash

# Скрипт для деплоя Kafka на VPS
# Использование: ./scripts/deploy-kafka.sh

set -e

VPS_IP="5.44.40.79"
VPS_USER="root"

echo "🚀 Деплоим Kafka на VPS $VPS_IP..."

# Проверяем подключение к VPS
echo "📡 Проверяем подключение к VPS..."
ssh -o ConnectTimeout=10 $VPS_USER@$VPS_IP "echo 'Подключение успешно'"

# Создаем директорию для проекта
echo "📁 Создаем директорию для проекта..."
ssh $VPS_USER@$VPS_IP "mkdir -p /opt/telegram-app"

# Копируем docker-compose файл
echo "📋 Копируем docker-compose файл..."
scp docker-compose.prod.yml $VPS_USER@$VPS_IP:/opt/telegram-app/

# Останавливаем старые контейнеры
echo "🛑 Останавливаем старые контейнеры..."
ssh $VPS_USER@$VPS_IP "cd /opt/telegram-app && docker-compose -f docker-compose.prod.yml down || true"

# Запускаем Kafka и Kafka UI
echo "🚀 Запускаем Kafka и Kafka UI..."
ssh $VPS_USER@$VPS_IP "cd /opt/telegram-app && docker-compose -f docker-compose.prod.yml up -d kafka kafka-ui"

# Ждем запуска Kafka
echo "⏳ Ждем запуска Kafka..."
sleep 30

# Проверяем статус контейнеров
echo "🔍 Проверяем статус контейнеров..."
ssh $VPS_USER@$VPS_IP "docker ps | grep -E '(kafka|telegram)'"

# Проверяем доступность Kafka
echo "📊 Проверяем доступность Kafka..."
ssh $VPS_USER@$VPS_IP "docker exec telegram_kafka_prod kafka-topics --bootstrap-server localhost:9092 --list || echo 'Kafka еще не готов'"

echo "🎉 Kafka успешно развернута!"
echo "🌐 Kafka UI доступен по адресу: http://$VPS_IP:8081"
echo "📝 Логи Kafka: ssh $VPS_USER@$VPS_IP 'docker logs telegram_kafka_prod -f'"
