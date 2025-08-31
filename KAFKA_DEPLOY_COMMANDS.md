# Команды для запуска Kafka на VPS

## 1. Подготовка VPS

```bash
# Подключение к VPS
ssh root@5.44.40.79

# Создание директории проекта
mkdir -p /opt/naidizakupku_telegram
cd /opt/naidizakupku_telegram
```

## 2. Копирование файлов

С локальной машины:
```bash
# Копируем docker-compose файл
scp docker-compose.prod.yml root@5.44.40.79:/opt/naidizakupku_telegram/

# Копируем .env файл (если есть)
scp .env root@5.44.40.79:/opt/naidizakupku_telegram/
```

## 3. Запуск Kafka

На VPS:
```bash
# Останавливаем старые контейнеры
docker-compose -f docker-compose.prod.yml down --remove-orphans

# Удаляем старые тома
docker volume rm naidizakupku_telegram_kafka_data || true

# Запускаем Kafka
docker-compose -f docker-compose.prod.yml up -d kafka

# Ждем запуска (30 секунд)
sleep 30

# Проверяем статус
docker ps | grep kafka

# Проверяем здоровье Kafka
docker exec telegram_kafka_prod kafka-topics --bootstrap-server localhost:9092 --list

# Запускаем Kafka UI
docker-compose -f docker-compose.prod.yml up -d kafka-ui

# Проверяем финальный статус
docker-compose -f docker-compose.prod.yml ps
```

## 4. Проверка работы

```bash
# Проверка Kafka UI
curl http://5.44.40.79:8081

# Проверка Kafka
telnet 5.44.40.79 9092

# Логи Kafka
docker logs telegram_kafka_prod -f

# Логи Kafka UI
docker logs telegram_kafka_ui_prod -f
```

## 5. Автозапуск при перезагрузке

```bash
# Создаем systemd сервис
cat > /etc/systemd/system/naidizakupku-telegram.service << 'EOF'
[Unit]
Description=Naidizakupku Telegram with Kafka
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/naidizakupku_telegram
ExecStart=/usr/local/bin/docker-compose -f docker-compose.prod.yml up -d
ExecStop=/usr/local/bin/docker-compose -f docker-compose.prod.yml down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
EOF

# Включаем автозапуск
systemctl daemon-reload
systemctl enable naidizakupku-telegram.service

# Проверяем статус
systemctl status naidizakupku-telegram
```

## 6. Обновление переменных окружения для приложения

После запуска Kafka нужно обновить переменные окружения в приложении:

```bash
# Останавливаем приложение
docker stop telegram-app || true

# Запускаем приложение с правильными переменными
docker run -d \
  --name telegram-app \
  --restart unless-stopped \
  -p 8080:8080 \
  -e POSTGRES_URL='jdbc:postgresql://5.44.40.79:5432/telegram_db' \
  -e POSTGRES_USER='postgres' \
  -e POSTGRES_PASSWORD='your_password' \
  -e KAFKA_BOOTSTRAP_SERVERS='5.44.40.79:9092' \
  -e SPRING_PROFILES_ACTIVE=prod \
  -v /opt/telegram-app/logs:/app/logs \
  ghcr.io/bjcreslin/naidizakupku-telegram:latest
```

## 7. Проверка полной работы

```bash
# Проверка приложения
curl http://5.44.40.79:8080/actuator/health

# Проверка Kafka UI
curl http://5.44.40.79:8081

# Проверка логов приложения
docker logs telegram-app -f
```

## 8. Troubleshooting

### Проблемы с Kafka
```bash
# Проверка логов Kafka
docker logs telegram_kafka_prod

# Перезапуск Kafka
docker-compose -f docker-compose.prod.yml restart kafka

# Проверка портов
netstat -tlnp | grep 9092
```

### Проблемы с подключением
```bash
# Проверка firewall
ufw status

# Открытие портов (если нужно)
ufw allow 9092
ufw allow 8081
ufw allow 8080
```

### Проблемы с томами
```bash
# Очистка томов
docker volume prune

# Пересоздание томов
docker-compose -f docker-compose.prod.yml down -v
docker-compose -f docker-compose.prod.yml up -d
```

## 9. Мониторинг

```bash
# Статус всех контейнеров
docker ps -a

# Использование ресурсов
docker stats

# Логи всех сервисов
docker-compose -f docker-compose.prod.yml logs -f
```
