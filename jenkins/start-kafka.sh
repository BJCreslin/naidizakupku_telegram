#!/bin/bash

# Скрипт для запуска Kafka на VPS
# Использование: ./start-kafka.sh

set -e

VPS_IP="5.44.40.79"
VPS_USER="root"
PROJECT_DIR="/opt/naidizakupku_telegram"

echo "🚀 Запускаем Kafka на VPS $VPS_IP..."

# Проверяем подключение к VPS
echo "📡 Проверяем подключение к VPS..."
ssh -o ConnectTimeout=10 $VPS_USER@$VPS_IP "echo 'Подключение успешно'"

# Создаем директорию проекта если не существует
echo "📁 Создаем директорию проекта..."
ssh $VPS_USER@$VPS_IP "mkdir -p $PROJECT_DIR"

# Копируем docker-compose файл
echo "📋 Копируем docker-compose.prod.yml..."
scp docker-compose.prod.yml $VPS_USER@$VPS_IP:$PROJECT_DIR/

# Останавливаем старые контейнеры Kafka
echo "🛑 Останавливаем старые контейнеры Kafka..."
ssh $VPS_USER@$VPS_IP "cd $PROJECT_DIR && docker-compose -f docker-compose.prod.yml stop kafka kafka-ui || true"
ssh $VPS_USER@$VPS_IP "cd $PROJECT_DIR && docker-compose -f docker-compose.prod.yml rm -f kafka kafka-ui || true"

# Удаляем старые тома Kafka
echo "🗑️ Удаляем старые тома Kafka..."
ssh $VPS_USER@$VPS_IP "docker volume rm naidizakupku_telegram_kafka_data || true"

# Запускаем Kafka
echo "🚀 Запускаем Kafka..."
ssh $VPS_USER@$VPS_IP "cd $PROJECT_DIR && docker-compose -f docker-compose.prod.yml up -d kafka"

# Ждем запуска Kafka
echo "⏳ Ждем запуска Kafka..."
sleep 30

# Проверяем статус Kafka
echo "🔍 Проверяем статус Kafka..."
ssh $VPS_USER@$VPS_IP "docker ps | grep kafka"

# Проверяем здоровье Kafka
echo "💚 Проверяем здоровье Kafka..."
ssh $VPS_USER@$VPS_IP "docker exec telegram_kafka_prod kafka-topics --bootstrap-server localhost:9092 --list || echo 'Kafka еще не готова'"

# Запускаем Kafka UI
echo "🖥️ Запускаем Kafka UI..."
ssh $VPS_USER@$VPS_IP "cd $PROJECT_DIR && docker-compose -f docker-compose.prod.yml up -d kafka-ui"

# Проверяем финальный статус
echo "🔍 Финальный статус контейнеров..."
ssh $VPS_USER@$VPS_IP "cd $PROJECT_DIR && docker-compose -f docker-compose.prod.yml ps"

echo "🎉 Kafka запущена!"
echo "📊 Kafka UI доступен по адресу: http://$VPS_IP:8081"
echo "🔌 Kafka доступна по адресу: $VPS_IP:9092"
echo "📝 Логи Kafka: ssh $VPS_USER@$VPS_IP 'docker logs telegram_kafka_prod -f'"
echo "📝 Логи Kafka UI: ssh $VPS_USER@$VPS_IP 'docker logs telegram_kafka_ui_prod -f'"
