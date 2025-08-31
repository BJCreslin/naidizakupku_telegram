# Naidizakupku Telegram Bot

Spring Boot приложение с интеграцией Apache Kafka для обработки событий и уведомлений.

## Технологии

- **Kotlin 2.0** + **Spring Boot 3.3**
- **PostgreSQL** + **Liquibase**
- **Apache Kafka** + **Zookeeper**
- **Docker** + **Docker Compose**
- **Gradle Kotlin DSL**

## Быстрый старт

### Локальная разработка

1. **Клонируйте репозиторий:**
```bash
git clone <repository-url>
cd naidizakupku_telegram
```

2. **Создайте файл .env:**
```bash
cp env.example .env
# Отредактируйте .env файл с вашими настройками
```

3. **Запустите локальную среду:**
```bash
# Запуск всех сервисов (PostgreSQL, Kafka, Zookeeper)
docker-compose up -d

# Или только Kafka
docker-compose up -d zookeeper kafka

# Запуск приложения
./gradlew bootRun
```

4. **Проверьте работу:**
- Приложение: http://localhost:8080
- Kafka UI: http://localhost:8081 (в продакшене)
- Health check: http://localhost:8080/actuator/health

### Продакшен деплой на VPS

1. **Подготовьте VPS:**
```bash
# Установите Docker и Docker Compose на VPS
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

2. **Деплой на VPS:**
```bash
# Запустите скрипт деплоя
./jenkins/deploy-kafka.sh
```

3. **Проверьте работу:**
- Приложение: http://5.44.40.79:8080
- Kafka UI: http://5.44.40.79:8081
- Kafka: 5.44.40.79:9092

## Архитектура

### Kafka Топики

- **user-events** - события пользователей (регистрация, обновление, удаление)
- **notifications** - уведомления для отправки пользователям

### Сервисы

- **KafkaService** - отправка сообщений в Kafka
- **KafkaConsumerService** - обработка сообщений из Kafka
- **UserService** - бизнес-логика пользователей

## API Endpoints

### Пользователи

```bash
# Создать пользователя
POST /api/users
{
  "telegramId": 123456789
}

# Получить пользователя
GET /api/users/{id}

# Обновить пользователя
PUT /api/users/{id}
{
  "telegramId": 123456789,
  "active": true
}

# Удалить пользователя
DELETE /api/users/{id}

# Отправить уведомление
POST /api/users/{id}/notify
{
  "message": "Тестовое уведомление",
  "type": "info"
}
```

## Конфигурация

### Переменные окружения

```bash
# База данных
POSTGRES_DB=telegram_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password
POSTGRES_URL=jdbc:postgresql://localhost:5432/telegram_db

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_USER=
KAFKA_PASSWORD=

# Приложение
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

### Docker Compose

- **docker-compose.yml** - для локальной разработки
- **docker-compose.prod.yml** - для продакшена с автозапуском

## Мониторинг

### Логи

```bash
# Логи приложения
docker-compose logs -f app

# Логи Kafka
docker-compose logs -f kafka

# Логи Zookeeper
docker-compose logs -f zookeeper
```

### Метрики

- Prometheus метрики: http://localhost:8080/actuator/prometheus
- Health check: http://localhost:8080/actuator/health

## Автозапуск на VPS

Приложение настроено на автозапуск при перезагрузке VPS через systemd:

```bash
# Проверить статус
sudo systemctl status naidizakupku-telegram

# Перезапустить
sudo systemctl restart naidizakupku-telegram

# Посмотреть логи
sudo journalctl -u naidizakupku-telegram -f
```

## Разработка

### Запуск тестов

```bash
./gradlew test
```

### Сборка

```bash
./gradlew build
```

### Docker образ

```bash
docker build -t naidizakupku-telegram .
```
