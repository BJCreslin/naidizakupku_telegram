#!/bin/bash

# Скрипт для деплоя Kafka на VPS (PostgreSQL установлен на VPS, Kafka в Docker)
# Использование: ./deploy-kafka.sh

set -e

VPS_IP="5.44.40.79"
VPS_USER="root"
PROJECT_DIR="/opt/naidizakupku_telegram"
DOCKER_COMPOSE_FILE="docker-compose.prod.yml"

echo "🚀 Начинаем деплой Kafka на VPS $VPS_IP (PostgreSQL на VPS, Kafka в Docker)..."

# Проверяем подключение к VPS
echo "📡 Проверяем подключение к VPS..."
ssh -o ConnectTimeout=10 $VPS_USER@$VPS_IP "echo 'Подключение успешно'"

# Проверяем PostgreSQL на VPS
echo "🐘 Проверяем PostgreSQL на VPS..."
ssh $VPS_USER@$VPS_IP "sudo systemctl status postgresql || echo 'PostgreSQL не найден, нужно установить'"

# Создаем директорию проекта если не существует
echo "📁 Создаем директорию проекта..."
ssh $VPS_USER@$VPS_IP "mkdir -p $PROJECT_DIR"

# Копируем docker-compose файл
echo "📋 Копируем docker-compose.prod.yml..."
scp docker-compose.prod.yml $VPS_USER@$VPS_IP:$PROJECT_DIR/

# Копируем .env файл если существует
if [ -f .env ]; then
    echo "🔐 Копируем .env файл..."
    scp .env $VPS_USER@$VPS_IP:$PROJECT_DIR/
fi

# Останавливаем старые контейнеры
echo "🛑 Останавливаем старые контейнеры..."
ssh $VPS_USER@$VPS_IP "cd $PROJECT_DIR && docker-compose -f $DOCKER_COMPOSE_FILE down --remove-orphans || true"

# Удаляем старые образы
echo "🗑️ Удаляем старые образы..."
ssh $VPS_USER@$VPS_IP "docker system prune -f"

# Запускаем Kafka и связанные сервисы
echo "🚀 Запускаем Kafka и связанные сервисы..."
ssh $VPS_USER@$VPS_IP "cd $PROJECT_DIR && docker-compose -f $DOCKER_COMPOSE_FILE up -d kafka kafka-ui"

# Ждем запуска Kafka
echo "⏳ Ждем запуска Kafka..."
sleep 30

# Проверяем статус контейнеров
echo "🔍 Проверяем статус контейнеров..."
ssh $VPS_USER@$VPS_IP "cd $PROJECT_DIR && docker-compose -f $DOCKER_COMPOSE_FILE ps"

# Проверяем здоровье Kafka
echo "💚 Проверяем здоровье Kafka..."
ssh $VPS_USER@$VPS_IP "docker exec telegram_kafka_prod kafka-topics --bootstrap-server localhost:9092 --list || echo 'Kafka еще не готова'"

# Останавливаем старое приложение
echo "🛑 Останавливаем старое приложение..."
ssh $VPS_USER@$VPS_IP "docker stop telegram-app || true"
ssh $VPS_USER@$VPS_IP "docker rm telegram-app || true"

# Запускаем приложение с правильными переменными окружения
echo "🚀 Запускаем приложение с Kafka..."
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
sleep 10

# Проверяем статус приложения
echo "🔍 Проверяем статус приложения..."
ssh $VPS_USER@$VPS_IP "docker ps | grep telegram-app"

# Настраиваем автозапуск Docker при перезагрузке
echo "⚙️ Настраиваем автозапуск Docker..."
ssh $VPS_USER@$VPS_IP "systemctl enable docker"

# Создаем systemd сервис для автозапуска проекта
echo "🔧 Создаем systemd сервис для автозапуска..."
ssh $VPS_USER@$VPS_IP "cat > /etc/systemd/system/naidizakupku-telegram.service << 'EOF'
[Unit]
Description=Naidizakupku Telegram with Kafka (PostgreSQL on VPS)
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=$PROJECT_DIR
ExecStart=/usr/local/bin/docker-compose -f $DOCKER_COMPOSE_FILE up -d
ExecStop=/usr/local/bin/docker-compose -f $DOCKER_COMPOSE_FILE down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
EOF"

# Включаем автозапуск
echo "✅ Включаем автозапуск сервиса..."
ssh $VPS_USER@$VPS_IP "systemctl daemon-reload && systemctl enable naidizakupku-telegram.service"

echo "🎉 Деплой завершен!"
echo "📊 Kafka UI доступен по адресу: http://$VPS_IP:8081"
echo "🔌 Kafka доступна по адресу: $VPS_IP:9092"
echo "🐘 PostgreSQL доступен по адресу: $VPS_IP:5432"
echo "🌐 Приложение доступно по адресу: http://$VPS_IP:8080"
echo "📝 Логи: ssh $VPS_USER@$VPS_IP 'docker logs telegram-app -f'"
echo ""
echo "⚠️  Важно:"
echo "   - PostgreSQL должен быть установлен и настроен на VPS"
echo "   - База данных telegram_db должна быть создана"
echo "   - Пользователь postgres должен иметь доступ к базе"
echo "   - Порт 5432 должен быть открыт для подключения из Docker"
echo "   - Kafka запущена в Docker и доступна на порту 9092"
