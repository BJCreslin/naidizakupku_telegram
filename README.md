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

3. **Настройте Telegram бота:**
   - Создайте бота через @BotFather в Telegram
   - Добавьте токен и данные бота в .env файл
   - Подробная инструкция: [TELEGRAM_BOT_SETUP.md](TELEGRAM_BOT_SETUP.md)

4. **Запустите локальную среду:**
```bash
# Запуск всех сервисов (PostgreSQL, Kafka, Zookeeper)
docker-compose up -d

# Или только Kafka
docker-compose up -d zookeeper kafka

# Запуск приложения
./gradlew bootRun
```

5. **Проверьте работу:**
- Приложение: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- API документация (JSON): http://localhost:8080/api-docs
- Telegram бот: найдите бота по username и отправьте сообщение
- Kafka UI: http://localhost:8081 (в продакшене)
- Health check: http://localhost:8080/actuator/health

### Устранение проблем

Если возникают проблемы с запуском, см. [DEPLOYMENT_FIX.md](DEPLOYMENT_FIX.md)

**Быстрое исправление проблем:**

**Проблема с JAXB:**
```bash
# Linux/macOS
./scripts/fix-jaxb-issue.sh

# Windows
scripts\fix-jaxb-issue.bat
```

**Проблема с подключением к БД:**
```bash
# Linux/macOS
./scripts/fix-database-connection.sh

# Windows
scripts\fix-database-connection.bat
```

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
./scripts/deploy-kafka.sh
```

3. **Проверьте работу:**
- Приложение: http://5.44.40.79:8080
- Kafka UI: http://5.44.40.79:8081
- Kafka: 5.44.40.79:9092

## Архитектура

Подробное описание архитектуры приложения находится в файле [ARCHITECTURE.md](ARCHITECTURE.md).

### Основные компоненты

- **TelegramBotService** - основной сервис Telegram бота с эхо-функцией
- **UserService** - бизнес-логика пользователей
- **KafkaService** - отправка сообщений в Kafka
- **KafkaConsumerService** - обработка сообщений из Kafka

### Kafka Топики

- **user-events** - события пользователей (регистрация, обновление, удаление)
- **notifications** - уведомления для отправки пользователям

### Поддержание документации

Для поддержания архитектурной документации в актуальном состоянии:

1. **Установите pre-commit hook:**
   - **Linux/macOS:** `./scripts/install-pre-commit.sh`
   - **Windows:** `scripts\install-pre-commit-windows.bat`

2. **Следуйте правилам** из [ARCHITECTURE_RULES.md](ARCHITECTURE_RULES.md)

3. **Обновляйте документацию** при изменении архитектуры

## API Endpoints

### Документация API

API документация доступна через Swagger UI:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

Swagger UI предоставляет интерактивный интерфейс для тестирования всех API endpoints с полным описанием параметров, примеров запросов и ответов.

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
  "message": "Уведомление",
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

# Telegram Bot
TELEGRAM_BOT_TOKEN=your_telegram_bot_token_here
TELEGRAM_BOT_NAME=your_telegram_bot_name_here
TELEGRAM_BOT_USERNAME=your_telegram_bot_username_here

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

## CI/CD Pipeline

Проект использует GitHub Actions для автоматического деплоя:

### Workflow

1. **Build** - сборка Docker образа при мерже в main
3. **Deploy** - автоматический деплой на VPS

### Secrets

Настройте следующие secrets в GitHub repository:

- `REMOTE_HOST` - IP адрес VPS (5.44.40.79)
- `REMOTE_USER` - пользователь для SSH (root)
- `SSH_PRIVATE_KEY` - приватный SSH ключ
- `GITFLIC_USER` - логин для GitFlic registry
- `GITFLIC_PASS` - пароль для GitFlic registry
- `POSTGRES_PASSWORD` - пароль от базы данных

### Быстрое исправление

Для быстрого исправления приложения используйте:

```bash
./scripts/fix-app.sh
```

## Автозапуск на VPS

Приложение настроено на автозапуск при перезагрузке VPS через Docker:

```bash
# Проверить статус
docker ps | grep telegram-app

# Перезапустить
docker restart telegram-app

# Посмотреть логи
docker logs telegram-app -f
```

## Разработка


### Сборка

```bash
./gradlew build
```

### Docker образ

```bash
docker build -t naidizakupku-telegram .
```
