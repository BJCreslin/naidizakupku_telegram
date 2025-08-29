#!/bin/bash

# Скрипт деплоя для удаленного Ubuntu сервера
# Используется в Jenkins pipeline

set -e

# Переменные окружения
APP_NAME="telegram-app"
DOCKER_IMAGE="naidizakupku/telegram-app"
DOCKER_TAG="${BUILD_NUMBER}"
REMOTE_PATH="/opt/telegram-app"
LOG_FILE="/var/log/telegram-app/deploy.log"

# Создаем директории если не существуют
sudo mkdir -p ${REMOTE_PATH}
sudo mkdir -p /var/log/telegram-app
sudo chown -R deploy:deploy ${REMOTE_PATH}
sudo chown -R deploy:deploy /var/log/telegram-app

# Логируем начало деплоя
echo "$(date): Starting deployment of ${DOCKER_IMAGE}:${DOCKER_TAG}" | tee -a ${LOG_FILE}

# Останавливаем и удаляем старый контейнер
echo "$(date): Stopping old container..." | tee -a ${LOG_FILE}
docker stop ${APP_NAME} 2>/dev/null || true
docker rm ${APP_NAME} 2>/dev/null || true

# Удаляем старый образ
echo "$(date): Removing old image..." | tee -a ${LOG_FILE}
docker rmi ${DOCKER_IMAGE}:latest 2>/dev/null || true

# Логинимся в Docker registry
echo "$(date): Logging into Docker registry..." | tee -a ${LOG_FILE}
echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin

# Скачиваем новый образ
echo "$(date): Pulling new image..." | tee -a ${LOG_FILE}
docker pull ${DOCKER_IMAGE}:${DOCKER_TAG}
docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest

# Создаем docker-compose файл для production
cat > ${REMOTE_PATH}/docker-compose.prod.yml << EOF
version: '3.8'
services:
  ${APP_NAME}:
    image: ${DOCKER_IMAGE}:${DOCKER_TAG}
    container_name: ${APP_NAME}
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - POSTGRES_URL=${POSTGRES_URL}
      - POSTGRES_USER=${POSTGRES_USER}
      - POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
      - KAFKA_BOOTSTRAP_SERVERS=${KAFKA_BOOTSTRAP_SERVERS}
    volumes:
      - ${REMOTE_PATH}/logs:/app/logs
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
EOF

# Запускаем новый контейнер
echo "$(date): Starting new container..." | tee -a ${LOG_FILE}
docker-compose -f ${REMOTE_PATH}/docker-compose.prod.yml up -d

# Ждем запуска приложения
echo "$(date): Waiting for application to start..." | tee -a ${LOG_FILE}
sleep 30

# Проверяем здоровье приложения
echo "$(date): Checking application health..." | tee -a ${LOG_FILE}
for i in {1..10}; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "$(date): Application is healthy!" | tee -a ${LOG_FILE}
        break
    else
        echo "$(date): Health check attempt $i failed, waiting..." | tee -a ${LOG_FILE}
        sleep 10
    fi
    
    if [ $i -eq 10 ]; then
        echo "$(date): Application failed to start properly!" | tee -a ${LOG_FILE}
        exit 1
    fi
done

# Очищаем старые образы
echo "$(date): Cleaning up old images..." | tee -a ${LOG_FILE}
docker image prune -f

echo "$(date): Deployment completed successfully!" | tee -a ${LOG_FILE}

