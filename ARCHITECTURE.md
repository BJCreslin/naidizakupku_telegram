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
- `TelegramService.kt` - основная логика обработки Telegram сообщений
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
- `TelegramConfig.kt` - настройки Telegram Bot

## Поток данных

```
Telegram Bot API → TelegramController → TelegramService → UserService → UserRepository → PostgreSQL
                                    ↓
                              KafkaService → Kafka → Асинхронная обработка
```

## Структура базы данных

### Таблица `users`
- `id` (BIGINT, PRIMARY KEY) - уникальный идентификатор
- `telegram_id` (BIGINT, UNIQUE) - ID пользователя в Telegram
- `created_at` (TIMESTAMP) - время создания записи
- `updated_at` (TIMESTAMP) - время последнего обновления

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

### Переменные окружения
- `POSTGRES_USER` - пользователь PostgreSQL
- `POSTGRES_PASSWORD` - пароль PostgreSQL
- `POSTGRES_URL` - URL подключения к PostgreSQL
- `KAFKA_BOOTSTRAP_SERVERS` - адреса Kafka серверов
- `KAFKA_USER` - пользователь Kafka
- `KAFKA_PASSWORD` - пароль Kafka
- `TELEGRAM_BOT_TOKEN` - токен Telegram бота

## Развертывание

### Локальная разработка
```bash
docker-compose up -d  # PostgreSQL + Kafka
./gradlew bootRun     # Приложение
```

### Продакшн
```bash
docker-compose -f docker-compose.prod.yml up -d
```

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
