# Архитектура приложения naidizakupku_telegram

## Обзор
Telegram-бот для поиска и покупки товаров с интеграцией Kafka для асинхронной обработки сообщений.

## Технологический стек
- **Язык**: Kotlin 2.0
- **Фреймворк**: Spring Boot 3.3
- **JDK**: 21
- **Сборка**: Gradle Kotlin DSL
- **База данных**: PostgreSQL + Spring Data JPA
- **Миграции**: Liquibase
- **Очереди сообщений**: Apache Kafka + Spring for Kafka
- **Тестирование**: Kotest + Mockk
- **Контейнеризация**: Docker
- **CI/CD**: GitHub Actions

## Архитектурные слои

### 1. Controller Layer (`controller/`)
**Назначение**: Обработка HTTP-запросов и Telegram webhook'ов
- `TelegramController.kt` - обработка входящих сообщений от Telegram Bot API

### 2. Service Layer (`service/`)
**Назначение**: Бизнес-логика приложения
- `TelegramBotService.kt` - основной сервис Telegram бота с эхо-функцией
- `UserService.kt` - управление пользователями
- `KafkaService.kt` - работа с Kafka для асинхронной обработки

### 3. Repository Layer (`repository/`)
**Назначение**: Доступ к данным
- `UserRepository.kt` - CRUD операции для пользователей

### 4. Domain Layer (`domain/`)
**Назначение**: Модели данных и бизнес-сущности
- `User.kt` - сущность пользователя

### 5. Config Layer (`config/`)
**Назначение**: Конфигурация приложения
- `KafkaConfig.kt` - настройки Kafka
- `TelegramConfig.kt` - настройки Telegram Bot (токен, имя, username)

## Поток данных

```
Telegram Bot API → TelegramBotService → UserService → UserRepository → PostgreSQL
                                    ↓
                              KafkaService → Kafka → Асинхронная обработка
```

## Структура базы данных

### База данных `telegram_db`
- **Тип**: PostgreSQL
- **Кодировка**: UTF-8
- **Владелец**: postgres (по умолчанию)

### Таблица `users`
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT) - уникальный идентификатор
- `telegram_id` (BIGINT, UNIQUE, NOT NULL) - ID пользователя в Telegram
- `first_name` (VARCHAR(255)) - имя пользователя
- `last_name` (VARCHAR(255)) - фамилия пользователя
- `username` (VARCHAR(255)) - username пользователя
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP) - время создания записи
- `updated_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP) - время последнего обновления
- `active` (BOOLEAN, DEFAULT TRUE) - статус активности пользователя

### Индексы
- `idx_users_telegram_id` - уникальный индекс по telegram_id
- `idx_users_username` - индекс по username (для быстрого поиска)
- `idx_users_active` - индекс по active (для фильтрации активных пользователей)

### Миграции
- **001-create-users-table.xml** - создание таблицы users
- **002-add-user-fields.xml** - добавление дополнительных полей
- **002-remove-user-fields.xml** - удаление неиспользуемых полей

## Kafka Topics

### Входящие сообщения
- `telegram.messages` - входящие сообщения от пользователей

### Исходящие сообщения
- `telegram.responses` - ответы пользователям
- `telegram.notifications` - уведомления

## Конфигурация

### Основные настройки (`application.yml`)
- Порт приложения: 8080
- Профили: dev, test, prod
- Логирование: SLF4J + Logback

### Конфигурация базы данных
- **PostgreSQL**: Основная БД для продакшна и разработки
- **H2**: In-memory БД для тестов
- **Liquibase**: Управление миграциями БД
- **JPA/Hibernate**: ORM с автоматическим определением диалекта
- **Подключение**: Через переменные окружения с fallback значениями

### Конфигурация Kafka
- **KRaft режим**: Без Zookeeper
- **Локальная разработка**: localhost:9092
- **Docker Compose**: kafka:29092
- **Аутентификация**: Опциональная через SASL/PLAIN

### Переменные окружения

#### База данных PostgreSQL
- `POSTGRES_DB` - название базы данных (по умолчанию: telegram_db)
- `POSTGRES_USER` - пользователь PostgreSQL (по умолчанию: postgres)
- `POSTGRES_PASSWORD` - пароль PostgreSQL (обязательно указать)
- `POSTGRES_URL` - полный URL подключения к PostgreSQL
  - Локальная разработка: `jdbc:postgresql://localhost:5432/telegram_db`
  - Docker Compose: `jdbc:postgresql://postgres:5432/telegram_db`
  - Продакшн: `jdbc:postgresql://host.docker.internal:5432/telegram_db`

#### Apache Kafka
- `KAFKA_BOOTSTRAP_SERVERS` - адреса Kafka серверов
  - Локальная разработка: `localhost:9092`
  - Docker Compose: `kafka:29092`
- `KAFKA_USER` - пользователь Kafka (опционально, для аутентификации)
- `KAFKA_PASSWORD` - пароль Kafka (опционально, для аутентификации)

#### Telegram Bot
- `TELEGRAM_BOT_TOKEN` - токен Telegram бота (обязательно)
- `TELEGRAM_BOT_NAME` - имя Telegram бота (опционально)
- `TELEGRAM_BOT_USERNAME` - username Telegram бота (опционально)

#### Приложение
- `SERVER_PORT` - порт приложения (по умолчанию: 8080)
- `SPRING_PROFILES_ACTIVE` - активный профиль Spring (dev/test/prod)

#### Продакшн (VPS)
- `VPS_IP` - IP адрес VPS сервера
- `VPS_USER` - пользователь для подключения к VPS

## Развертывание

### Локальная разработка
```bash
docker-compose up -d  # PostgreSQL + Kafka
./gradlew bootRun     # Приложение
```

### Продакшн
```bash
# Автоматический деплой через GitHub Actions
# Приложение запускается в Docker контейнере и подключается к локальной PostgreSQL
```

**Важно**: В продакшне PostgreSQL должен быть запущен локально на хосте, а приложение в контейнере подключается к нему через `host.docker.internal:5432`.

### Исправления проблем развертывания
- **Проблема с подключением к БД**: Исправлено использование `host.docker.internal` вместо IP адреса
- **Убран явный диалект PostgreSQL**: Hibernate теперь автоматически определяет диалект
- **Добавлен --add-host**: Для поддержки `host.docker.internal` на Linux в GitHub Actions

## Мониторинг и логирование
- Логи приложения: `logs/application.log`
- Метрики Spring Boot Actuator
- Health checks для всех компонентов

## Безопасность
- Чувствительные данные в переменных окружения
- Валидация входящих данных
- Логирование действий пользователей
- Rate limiting для API endpoints

## Тестирование
- Unit тесты: Kotest + Mockk
- Integration тесты: TestContainers
- End-to-end тесты: Telegram Bot API моки

## CI/CD Pipeline
- Автоматическая сборка при изменении кода
- Запуск тестов
- Сборка Docker образа
- Деплой на Ubuntu 20.04 сервер
