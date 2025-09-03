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
- **Планировщик**: Spring Scheduling для автоматических задач
- **Тестирование**: Kotest + Mockk
- **Контейнеризация**: Docker
- **CI/CD**: GitHub Actions

## Архитектурные слои

### Структура файлов для функционала кодов
```
src/main/kotlin/com/naidizakupku/telegram/
├── domain/
│   └── UserCode.kt                    # Сущность временного кода
├── repository/
│   └── UserCodeRepository.kt          # Репозиторий для работы с кодами
├── service/
│   ├── UserCodeService.kt             # Основной сервис управления кодами
│   └── CodeGenerationService.kt       # Сервис генерации уникальных кодов
├── handler/
│   └── TelegramCodeHandler.kt         # Обработчик команды /code
├── scheduler/
│   └── CodeCleanupScheduler.kt        # Планировщик очистки кодов
└── config/
    └── TelegramConfig.kt              # Конфигурация времени действия кодов

src/main/resources/
├── application.yml                     # Конфигурация telegram.code.expiration.minutes
└── db/changelog/
    └── changes/
        └── 002-create-user-codes-table.xml  # Миграция таблицы user_codes

src/test/kotlin/com/naidizakupku/telegram/service/
├── UserCodeServiceTest.kt             # Тесты для UserCodeService
└── CodeGenerationServiceTest.kt       # Тесты для CodeGenerationService
```

### 1. Controller Layer (`controller/`)
**Назначение**: Обработка HTTP-запросов и Telegram webhook'ов
- `TelegramController.kt` - обработка входящих сообщений от Telegram Bot API

### 2. Service Layer (`service/`)
**Назначение**: Бизнес-логика приложения
- `TelegramBotService.kt` - основной сервис Telegram бота с эхо-функцией и обработкой команды /code
- `UserService.kt` - управление пользователями
- `UserCodeService.kt` - управление временными кодами пользователей
- `CodeGenerationService.kt` - генерация уникальных 7-значных кодов
- `KafkaService.kt` - отправка сообщений в Kafka топики
- `KafkaConsumerService.kt` - потребление сообщений из Kafka топиков
- `StartupInfoService.kt` - отображение параметров подключений и интеграций при старте приложения
- `DatabaseHealthService.kt` - проверка здоровья базы данных и проверка таблиц

### 3. Repository Layer (`repository/`)
**Назначение**: Доступ к данным
- `UserRepository.kt` - CRUD операции для пользователей
- `UserCodeRepository.kt` - CRUD операции для временных кодов пользователей

### 4. Domain Layer (`domain/`)
**Назначение**: Модели данных и бизнес-сущности
- `User.kt` - сущность пользователя
- `UserCode.kt` - сущность временного кода пользователя

### 5. Config Layer (`config/`)
**Назначение**: Конфигурация приложения
- `KafkaConfig.kt` - настройки Kafka, создание топиков (user-events, notifications)
- `TelegramConfig.kt` - настройки Telegram Bot (токен, имя, username)

### 6. Handler Layer (`handler/`)
**Назначение**: Обработчики команд Telegram бота
- `TelegramCodeHandler.kt` - обработчик команды /code для генерации временных кодов

### 7. Scheduler Layer (`scheduler/`)
**Назначение**: Планировщики задач
- `CodeCleanupScheduler.kt` - автоматическая очистка просроченных кодов каждые 5 минут (300000 мс)

## Поток данных

### Основной поток
```
Telegram Bot API → TelegramBotService → UserService → UserRepository → PostgreSQL
                                    ↓
                              KafkaService → Kafka → KafkaConsumerService

Команда /code:
Telegram Bot API → TelegramBotService → TelegramCodeHandler → UserCodeService → CodeGenerationService → UserCodeRepository → PostgreSQL
```

### Kafka потоки
- **Отправка событий**: `KafkaService.sendUserEvent()` → `user-events` топик
- **Отправка уведомлений**: `KafkaService.sendNotification()` → `notifications` топик
- **Потребление событий**: `user-events` топик → `KafkaConsumerService.handleUserEvent()`
- **Потребление уведомлений**: `notifications` топик → `KafkaConsumerService.handleNotification()`

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

### Таблица `user_codes`
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT) - уникальный идентификатор
- `code` (VARCHAR(7), UNIQUE, NOT NULL) - 7-значный уникальный код
- `telegram_user_id` (BIGINT, NOT NULL) - ID пользователя Telegram
- `expires_at` (TIMESTAMP, NOT NULL) - время истечения кода
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP) - время создания кода

### Индексы
- `idx_users_telegram_id` - уникальный индекс по telegram_id
- `idx_users_username` - индекс по username (для быстрого поиска)
- `idx_users_active` - индекс по active (для фильтрации активных пользователей)

#### Индексы для таблицы `user_codes`
- `idx_user_codes_code` - уникальный индекс по code для быстрого поиска
- `idx_user_codes_telegram_user_id` - индекс по telegram_user_id для поиска кодов пользователя
- `idx_user_codes_expires_at` - индекс по expires_at для очистки просроченных кодов

### Миграции
- **001-create-users-table.xml** - создание таблицы users
- **002-create-user-codes-table.xml** - создание таблицы user_codes для временных кодов
- **002-add-user-fields.xml** - добавление дополнительных полей
- **002-remove-user-fields.xml** - удаление неиспользуемых полей

## Kafka Topics

### Топики для событий пользователей
- `user-events` - события пользователей (регистрация, вход, выход)
  - **Структура сообщения**: JSON с полями `userId`, `eventType`, `timestamp`, `data`
  - **Типы событий**: `user_registered`, `user_login`, `user_logout`
  - **Партиции**: 3
  - **Реплики**: 1

### Топики для уведомлений
- `notifications` - уведомления пользователям
  - **Структура сообщения**: JSON с полями `userId`, `message`, `type`, `timestamp`
  - **Типы уведомлений**: `info`, `warning`, `error`
  - **Партиции**: 3
  - **Реплики**: 1

### Конфигурация топиков
- **Consumer Group**: `naidizakupku-telegram-consumer`
- **Auto Offset Reset**: `earliest`
- **Enable Auto Commit**: `false` (ручное подтверждение)
- **Max Poll Records**: 500
- **Session Timeout**: 30000ms
- **Heartbeat Interval**: 3000ms

### Команды создания топиков
```bash
# Создание топика user-events
/opt/kafka/bin/kafka-topics.sh \
  --create \
  --bootstrap-server 5.44.40.79:9092 \
  --replication-factor 1 \
  --partitions 3 \
  --config compression.type=zstd \
  --config retention.ms=604800000 \
  --config max.message.bytes=10485760 \
  --topic user-events

# Создание топика notifications
/opt/kafka/bin/kafka-topics.sh \
  --create \
  --bootstrap-server 5.44.40.79:9092 \
  --replication-factor 1 \
  --partitions 3 \
  --config compression.type=zstd \
  --config retention.ms=604800000 \
  --config max.message.bytes=10485760 \
  --topic notifications
```

### Настройки топиков
- **Compression**: zstd (высокая степень сжатия)
- **Retention**: 7 дней (604800000 мс)
- **Max Message Size**: 10MB (10485760 байт)

## Конфигурация

### Основные настройки (`application.yml`)
- Порт приложения: 8080
- Профили: dev, test, prod
- Логирование: SLF4J + Logback
- Планировщик: @EnableScheduling для автоматических задач

### Продакшн профиль (`application-prod.yml`)
- Пониженный уровень логирования Kafka (WARN вместо INFO)
- Отключены SQL логи Hibernate
- Ограниченные endpoints мониторинга (только health, info)
- Увеличен размер лог-файлов (50MB) с сокращенной историей (7 дней)

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

### Конфигурация временных кодов
- **Время действия**: Настраивается через `telegram.code.expiration.minutes` (по умолчанию: 5 минут)
- **Формат кода**: 7 цифр от 1000000 до 9999999 (первая цифра не 0)
- **Уникальность**: Каждый код уникален в системе
- **Очистка**: Автоматическое удаление просроченных кодов каждые 5 минут

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

#### Временные коды
- `TELEGRAM_CODE_EXPIRATION_MINUTES` - время действия кода в минутах (по умолчанию: 5)

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
- **StartupInfoService** - автоматическое логирование параметров подключений при старте приложения

### Мониторинг кодов
- **Метрики генерации**: Количество созданных кодов, время генерации
- **Метрики очистки**: Количество удаленных просроченных кодов
- **Алерты**: Уведомления при превышении лимита попыток генерации
- **Dashboard**: Статистика использования команды /code

### Уровни логирования по профилям
- **dev**: INFO для всех компонентов, включая Kafka и Hibernate
- **prod**: WARN для Kafka, Hibernate и Spring Framework, INFO для основного приложения
- **test**: INFO для всех компонентов

### Логирование операций с кодами
- Генерация новых кодов: INFO уровень с деталями (пользователь, код, время истечения)
- Поиск существующих кодов: INFO уровень
- Очистка просроченных кодов: INFO уровень с количеством удаленных записей
- Ошибки генерации: ERROR уровень с деталями

### Технические детали реализации
- **Генерация кодов**: ThreadLocalRandom для криптографически безопасной генерации
- **Проверка уникальности**: Оптимизированные SQL запросы с индексами
- **Временные метки**: LocalDateTime с автоматическим определением часового пояса
- **Транзакции**: Spring Data JPA транзакции для атомарности операций

### Информация при старте
При запуске приложения автоматически выводится:
- Активный профиль и порт сервера
- Параметры подключения к базе данных (маскированные)
- Настройки Kafka (серверы, протокол безопасности)
- Конфигурация Telegram Bot (username, токен маскирован)
- Статус готовности всех компонентов

### Команды Telegram бота
- `/code` - генерация временного 7-значного кода для пользователя
  - Если у пользователя есть активный код - возвращает существующий
  - Если нет - генерирует новый уникальный код
  - Показывает время действия в часовом поясе МСК (UTC+3)
  - Формат ответа: 🔑 Ваш код: 1234567 ⏰ Действителен до: 15:30 (МСК)

## Безопасность
- Чувствительные данные в переменных окружения
- Валидация входящих данных
- Логирование действий пользователей
- Rate limiting для API endpoints

### Безопасность кодов
- **Уникальность**: Гарантированная уникальность каждого кода в системе
- **Временное ограничение**: Автоматическое истечение кодов
- **Изоляция**: Коды привязаны к конкретному пользователю
- **Логирование**: Полная аудитория всех операций с кодами

## Валидация и обработка ошибок

### Валидация кодов
- Проверка уникальности: Автоматическая генерация нового кода при конфликте
- Максимальное количество попыток: 100 для генерации уникального кода
- Валидация формата: Строго 7 цифр от 1000000 до 9999999

### Обработка исключений
- Ошибки БД: Логирование с деталями, возврат пользователю понятного сообщения
- Ошибки генерации: Fallback на повторные попытки с ограничением
- Недоступность БД: Graceful degradation с логированием ошибок

### Обработка часовых поясов
- По умолчанию: UTC+3 (Московское время)
- Fallback: При ошибке определения часового пояса пользователя
- Форматирование: Время в формате HH:mm с указанием часового пояса

## Тестирование
- Unit тесты: Kotest + Mockk
  - `UserCodeServiceTest.kt` - тесты для сервиса управления кодами
  - `CodeGenerationServiceTest.kt` - тесты для сервиса генерации кодов
- Integration тесты: TestContainers
- End-to-end тесты: Telegram Bot API моки

## CI/CD Pipeline
- Автоматическая сборка при изменении кода
- Запуск тестов
- Сборка Docker образа
- Деплой на Ubuntu 20.04 сервер

## Производительность

### Оптимизация работы с кодами
- **Индексы БД**: Оптимизированные запросы по code, telegram_user_id, expires_at
- **Кэширование**: Проверка существующих кодов перед генерацией новых
- **Batch операции**: Массовое удаление просроченных кодов
- **Планировщик**: Оптимальная частота очистки (каждые 5 минут)

### Масштабируемость
- **Уникальность кодов**: Алгоритм с ограничением попыток для предотвращения бесконечных циклов
- **Очистка данных**: Автоматическое освобождение места в БД
- **Асинхронность**: Неблокирующая генерация кодов

### Исправления деплоя
- **Проблема с форматом переменных окружения**: Исправлены пробелы вокруг `=` в команде `docker run`
- **Добавлены переменные Kafka**: `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_USER`, `KAFKA_PASSWORD`
- **Проверка переменных окружения**: Добавлена валидация всех необходимых переменных перед деплоем
- **Улучшенная обработка ошибок**: Более детальные сообщения об ошибках и логирование

## Примеры использования

### Команда /code
```
Пользователь: /code
Бот: 🔑 Ваш код: 1234567
     ⏰ Действителен до: 15:30 (МСК)

Пользователь: /code (повторно)
Бот: 🔑 Ваш код: 1234567
     ⏰ Действителен до: 15:30 (МСК)
```

### Логирование операций
```
2024-01-01 15:25:00 INFO  - Создан код для пользователя 123: 1234567, истекает в 2024-01-01T15:30:00
2024-01-01 15:25:05 INFO  - Найден существующий код для пользователя 123: 1234567
2024-01-01 15:30:00 INFO  - Удалено 1 просроченных кодов
```
