# Настройка Apache Kafka

## Архитектура

### PostgreSQL на VPS
- PostgreSQL установлен напрямую на VPS (не в Docker)
- Приложение подключается к PostgreSQL по адресу `5.44.40.79:5432`
- База данных `telegram_db` должна быть создана на VPS

### Kafka без Zookeeper (KRaft режим)
- Kafka работает в KRaft режиме (без Zookeeper)
- Использует встроенный контроллер для управления метаданными
- Более простая и современная архитектура

## Что добавлено

### 1. Локальная разработка
- **docker-compose.yml** - обновлен с Kafka в KRaft режиме
- **KafkaService** - сервис для отправки сообщений
- **KafkaConsumerService** - сервис для обработки сообщений
- **KafkaConfig** - конфигурация Producer и Consumer
- **Обновленный UserController** - интеграция с Kafka

### 2. Продакшен деплой
- **docker-compose.prod.yml** - продакшен конфигурация с Kafka в KRaft режиме
- **deploy-kafka.sh** - скрипт деплоя на VPS
- **application-prod.yml** - продакшен настройки приложения

## Быстрый старт

### Локальная разработка

1. **Запуск Kafka:**
```bash
# Запуск Kafka в KRaft режиме
docker-compose up -d kafka kafka-ui

# Или все сервисы
docker-compose up -d
```

2. **Проверка работы:**
- Kafka UI: http://localhost:8081
- Kafka: localhost:9092

3. **Запуск приложения:**
```bash
./gradlew bootRun
```

### Продакшен деплой на VPS

1. **Подготовка VPS:**
```bash
# Установка Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Установка Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Установка PostgreSQL (если не установлен)
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl enable postgresql
sudo systemctl start postgresql

# Создание базы данных
sudo -u postgres psql -c "CREATE DATABASE telegram_db;"
sudo -u postgres psql -c "CREATE USER postgres WITH PASSWORD 'your_password';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE telegram_db TO postgres;"
```

2. **Деплой:**
```bash
# Создайте .env файл с настройками
cp env.example .env
# Отредактируйте .env

# Запустите деплой
./scripts/deploy-kafka.sh
```

3. **Проверка:**
- Приложение: http://5.44.40.79:8080
- Kafka UI: http://5.44.40.79:8081
- Kafka: 5.44.40.79:9092
- PostgreSQL: 5.44.40.79:5432

## Архитектура Kafka

### KRaft режим
- **Node ID**: 1
- **Process Roles**: broker,controller
- **Controller Quorum Voters**: 1@kafka:29093
- **Cluster ID**: 4L6g3nShT-eMCtK--X86sw

### Топики
- **user-events** - события пользователей (3 партиции, 1 реплика)
- **notifications** - уведомления (3 партиции, 1 реплика)

### Сервисы
- **KafkaService** - отправка сообщений в топики
- **KafkaConsumerService** - обработка сообщений из топиков
- **KafkaConfig** - конфигурация Producer/Consumer

## API Endpoints

### Пользователи с Kafka событиями

```bash
# Создать пользователя (отправляет user_registered событие)
POST /api/users
{
  "telegramId": 123456789
}

# Обновить пользователя (отправляет user_updated событие)
PUT /api/users/{id}
{
  "telegramId": 123456789,
  "active": true
}

# Удалить пользователя (отправляет user_deleted событие)
DELETE /api/users/{id}

# Отправить уведомление
POST /api/users/{id}/notify
{
  "message": "Уведомление",
  "type": "info"
}
```

## Мониторинг

### Логи
```bash
# Логи Kafka
docker-compose logs -f kafka

# Логи приложения
docker-compose logs -f app

# Логи на VPS
ssh root@5.44.40.79 "docker-compose -f /opt/naidizakupku_telegram/docker-compose.prod.yml logs -f"
```

### Kafka UI
- Локально: http://localhost:8081
- Продакшен: http://5.44.40.79:8081

### Health Checks
```bash
# Приложение
curl http://localhost:8080/actuator/health

# Kafka
docker exec telegram_kafka kafka-topics --bootstrap-server localhost:9092 --list

# PostgreSQL
sudo -u postgres pg_isready -h localhost -p 5432
```

## Автозапуск на VPS

Приложение настроено на автозапуск через systemd:

```bash
# Проверить статус
sudo systemctl status naidizakupku-telegram

# Перезапустить
sudo systemctl restart naidizakupku-telegram

# Логи
sudo journalctl -u naidizakupku-telegram -f
```

## Конфигурация

### Переменные окружения
```bash
# PostgreSQL (на VPS)
POSTGRES_DB=telegram_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password
POSTGRES_URL=jdbc:postgresql://5.44.40.79:5432/telegram_db

# Kafka (KRaft режим)
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_USER=
KAFKA_PASSWORD=
```

### Docker Compose файлы
- **docker-compose.yml** - локальная разработка
- **docker-compose.prod.yml** - продакшен с автозапуском


## Troubleshooting

### Проблемы с подключением к Kafka
1. Проверьте, что Kafka запущен в KRaft режиме
2. Проверьте логи Kafka: `docker-compose logs kafka`
3. Проверьте порты: `netstat -an | grep 9092`

### Проблемы с PostgreSQL
1. Проверьте статус PostgreSQL: `systemctl status postgresql`
2. Проверьте подключение: `sudo -u postgres psql -d telegram_db`
3. Проверьте права доступа пользователя postgres

### Проблемы с автозапуском на VPS
1. Проверьте systemd сервис: `systemctl status naidizakupku-telegram`
2. Проверьте логи: `journalctl -u naidizakupku-telegram -f`
3. Проверьте Docker: `systemctl status docker`

### Проблемы с топиками
1. Проверьте создание топиков в Kafka UI
2. Проверьте логи приложения на ошибки создания топиков
3. Проверьте права доступа к Kafka

## Безопасность

### Продакшен настройки
- Используйте переменные окружения для паролей
- Настройте firewall на VPS
- Используйте SSL/TLS для Kafka в продакшене
- Настройте SASL аутентификацию при необходимости
- Ограничьте доступ к PostgreSQL только с Docker сети

### Мониторинг безопасности
- Логируйте все события Kafka
- Мониторьте доступ к Kafka UI
- Регулярно обновляйте Docker образы
- Мониторьте подключения к PostgreSQL
