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

```

### Структура файлов для функционала верификации
```
src/main/kotlin/com/naidizakupku/telegram/
├── domain/
│   ├── dto/
│   │   └── CodeVerificationDto.kt     # DTO классы для Kafka сообщений
│   └── entity/
│       └── VerificationSession.kt     # Сущность сессии верификации
├── repository/
│   └── VerificationSessionRepository.kt # Репозиторий для сессий верификации
├── service/
│   ├── KafkaVerificationService.kt    # Основная логика верификации
│   ├── VerificationSessionService.kt  # Управление сессиями
│   ├── TelegramNotificationService.kt # Отправка уведомлений в Telegram
│   └── KafkaProducerService.kt        # Отправка сообщений в Kafka
├── handler/
│   └── VerificationCallbackHandler.kt # Обработка нажатий кнопок
├── listener/
│   ├── VerificationRequestListener.kt # Kafka listener для запросов
│   └── RevokeResponseListener.kt      # Kafka listener для ответов отзыва
├── scheduler/
│   └── VerificationSessionCleanupScheduler.kt # Очистка просроченных сессий
├── controller/
│   └── VerificationController.kt      # REST API для верификации
└── config/
    └── KafkaConfig.kt                 # Конфигурация Kafka для верификации

src/main/resources/
├── application.yml                     # Конфигурация Kafka и верификации
└── db/changelog/
    └── changes/
        └── 003-create-verification-sessions-table.xml # Миграция таблицы verification_sessions

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
- `KafkaVerificationService.kt` - основная логика верификации кодов через Kafka
- `VerificationSessionService.kt` - управление сессиями верификации
- `TelegramNotificationService.kt` - отправка уведомлений в Telegram для верификации
- `KafkaProducerService.kt` - отправка сообщений в Kafka топики верификации

### 3. Repository Layer (`repository/`)
**Назначение**: Доступ к данным
- `UserRepository.kt` - CRUD операции для пользователей
- `UserCodeRepository.kt` - CRUD операции для временных кодов пользователей
- `VerificationSessionRepository.kt` - CRUD операции для сессий верификации

### 4. Domain Layer (`domain/`)
**Назначение**: Модели данных и бизнес-сущности
- `User.kt` - сущность пользователя
- `UserCode.kt` - сущность временного кода пользователя
- `VerificationSession.kt` - сущность сессии верификации кода
- `VerificationStatus.kt` - enum статусов верификации (PENDING, CONFIRMED, REVOKED)

### 5. Config Layer (`config/`)
**Назначение**: Конфигурация приложения
- `KafkaConfig.kt` - настройки Kafka, создание топиков (user-events, notifications, верификация)
- `TelegramConfig.kt` - настройки Telegram Bot (токен, имя, username)

### 6. Handler Layer (`handler/`)
**Назначение**: Обработчики команд Telegram бота
- `TelegramCodeHandler.kt` - обработчик команды /code для генерации временных кодов
- `VerificationCallbackHandler.kt` - обработчик callback'ов для кнопок верификации

### 7. Scheduler Layer (`scheduler/`)
**Назначение**: Планировщики задач
- `CodeCleanupScheduler.kt` - автоматическая очистка просроченных кодов каждые 5 минут (300000 мс)
- `VerificationSessionCleanupScheduler.kt` - автоматическая очистка просроченных сессий верификации каждые 5 минут

## Поток данных

### Основной поток
```
Telegram Bot API → TelegramBotService → UserService → UserRepository → PostgreSQL
                                    ↓
                              KafkaService → Kafka → KafkaConsumerService

Команда /code:
Telegram Bot API → TelegramBotService → TelegramCodeHandler → UserCodeService → CodeGenerationService → UserCodeRepository → PostgreSQL

Верификация кодов:
Kafka → VerificationRequestListener → KafkaVerificationService → VerificationSessionService → TelegramNotificationService → Telegram Bot API
```

### Kafka потоки
- **Отправка событий**: `KafkaService.sendUserEvent()` → `user-events` топик
- **Отправка уведомлений**: `KafkaService.sendNotification()` → `notifications` топик
- **Потребление событий**: `user-events` топик → `KafkaConsumerService.handleUserEvent()`
- **Потребление уведомлений**: `notifications` топик → `KafkaConsumerService.handleNotification()`

### Kafka потоки верификации
- **Запросы верификации**: `code-verification-request` топик → `VerificationRequestListener` → `KafkaVerificationService`
- **Ответы верификации**: `KafkaProducerService` → `code-verification-response` топик
- **Запросы отзыва**: `KafkaProducerService` → `authorization-revoke-request` топик
- **Ответы отзыва**: `authorization-revoke-response` топик → `RevokeResponseListener` → `KafkaVerificationService`

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

### Таблица `verification_sessions`
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT) - уникальный идентификатор
- `correlation_id` (VARCHAR(36), UNIQUE, NOT NULL) - UUID для связывания запросов и ответов
- `telegram_user_id` (BIGINT, NOT NULL) - ID пользователя в Telegram
- `code` (VARCHAR(7), NOT NULL) - код авторизации (7 символов)
- `browser_info` (JSON) - информация о браузере в формате JSON
- `status` (VARCHAR(20), DEFAULT 'PENDING') - статус сессии: PENDING, CONFIRMED, REVOKED
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP) - время создания сессии
- `updated_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP) - время последнего обновления сессии

### Индексы
- `idx_users_telegram_id` - уникальный индекс по telegram_id
- `idx_users_username` - индекс по username (для быстрого поиска)
- `idx_users_active` - индекс по active (для фильтрации активных пользователей)

#### Индексы для таблицы `user_codes`
- `idx_user_codes_code` - уникальный индекс по code для быстрого поиска
- `idx_user_codes_telegram_user_id` - индекс по telegram_user_id для поиска кодов пользователя
- `idx_user_codes_expires_at` - индекс по expires_at для очистки просроченных кодов

#### Индексы для таблицы `verification_sessions`
- `idx_verification_correlation_id` - уникальный индекс по correlation_id для быстрого поиска
- `idx_verification_telegram_user_id` - индекс по telegram_user_id для поиска сессий пользователя
- `idx_verification_status_created` - составной индекс по status и created_at для очистки просроченных сессий

### Миграции
- **001-create-users-table.xml** - создание таблицы users
- **002-create-user-codes-table.xml** - создание таблицы user_codes для временных кодов
- **003-create-verification-sessions-table.xml** - создание таблицы verification_sessions для сессий верификации
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

### Топики для верификации кодов
- `code-verification-request` - входящие запросы на верификацию
  - **Структура сообщения**: JSON с полями `correlationId`, `code`, `userBrowserInfo`, `timestamp`
  - **Consumer Group**: `telegram-bot-verification`
  - **Auto Offset Reset**: `latest`
- `code-verification-response` - ответы на верификацию
  - **Структура сообщения**: JSON с полями `correlationId`, `success`, `telegramUserId`, `message`, `timestamp`
- `authorization-revoke-request` - запросы на отзыв авторизации
  - **Структура сообщения**: JSON с полями `correlationId`, `telegramUserId`, `originalVerificationCorrelationId`, `reason`, `timestamp`
- `authorization-revoke-response` - подтверждение отзыва авторизации
  - **Структура сообщения**: JSON с полями `correlationId`, `originalVerificationCorrelationId`, `telegramUserId`, `success`, `message`, `timestamp`

### Конфигурация топиков
- **Consumer Group**: `naidizakupku-telegram-consumer`
- **Auto Offset Reset**: `earliest`
- **Enable Auto Commit**: `false` (ручное подтверждение)
- **Max Poll Records**: 500
- **Session Timeout**: 30000ms
- **Heartbeat Interval**: 3000ms

### Конфигурация топиков верификации
- **Consumer Group**: `telegram-bot-verification`
- **Auto Offset Reset**: `latest`
- **Enable Auto Commit**: `false` (ручное подтверждение)
- **Consumer Group для отзыва**: `telegram-bot-verification-revoke`

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

# Создание топика code-verification-request
/opt/kafka/bin/kafka-topics.sh \
  --create \
  --bootstrap-server 5.44.40.79:9092 \
  --replication-factor 1 \
  --partitions 3 \
  --config compression.type=zstd \
  --config retention.ms=604800000 \
  --config max.message.bytes=10485760 \
  --topic code-verification-request

# Создание топика code-verification-response
/opt/kafka/bin/kafka-topics.sh \
  --create \
  --bootstrap-server 5.44.40.79:9092 \
  --replication-factor 1 \
  --partitions 3 \
  --config compression.type=zstd \
  --config retention.ms=604800000 \
  --config max.message.bytes=10485760 \
  --topic code-verification-response

# Создание топика authorization-revoke-request
/opt/kafka/bin/kafka-topics.sh \
  --create \
  --bootstrap-server 5.44.40.79:9092 \
  --replication-factor 1 \
  --partitions 3 \
  --config compression.type=zstd \
  --config retention.ms=604800000 \
  --config max.message.bytes=10485760 \
  --topic authorization-revoke-request

# Создание топика authorization-revoke-response
/opt/kafka/bin/kafka-topics.sh \
  --create \
  --bootstrap-server 5.44.40.79:9092 \
  --replication-factor 1 \
  --partitions 3 \
  --config compression.type=zstd \
  --config retention.ms=604800000 \
  --config max.message.bytes=10485760 \
  --topic authorization-revoke-response
```

### Настройки топиков
- **Compression**: zstd (высокая степень сжатия)
- **Retention**: 7 дней (604800000 мс)
- **Max Message Size**: 10MB (10485760 байт)

## Конфигурация

### Основные настройки (`application.yml`)
- Порт приложения: 8080
- Профили: dev, prod
- Логирование: SLF4J + Logback
- Планировщик: @EnableScheduling для автоматических задач
- Kafka верификация: Отдельная конфигурация для топиков верификации

### Продакшн профиль (`application-prod.yml`)
- Пониженный уровень логирования Kafka (WARN вместо INFO)
- Отключены SQL логи Hibernate
- Ограниченные endpoints мониторинга (только health, info)
- Увеличен размер лог-файлов (50MB) с сокращенной историей (7 дней)

### Конфигурация базы данных
- **PostgreSQL**: Основная БД для продакшна и разработки
- **Liquibase**: Управление миграциями БД
- **JPA/Hibernate**: ORM с автоматическим определением диалекта
- **Подключение**: Через переменные окружения с fallback значениями

### Конфигурация Kafka
- **KRaft режим**: Без Zookeeper
- **Локальная разработка**: localhost:9092
- **Docker Compose**: kafka:29092
- **Аутентификация**: Опциональная через SASL/PLAIN
- **Верификация**: Отдельные consumer groups для разных типов сообщений

### Конфигурация временных кодов
- **Время действия**: Настраивается через `telegram.code.expiration.minutes` (по умолчанию: 5 минут)
- **Формат кода**: 7 цифр от 1000000 до 9999999 (первая цифра не 0)
- **Уникальность**: Каждый код уникален в системе
- **Очистка**: Автоматическое удаление просроченных кодов каждые 5 минут

### Конфигурация верификации
- **Время жизни сессий**: Настраивается через `verification.session.cleanup.minutes` (по умолчанию: 30 минут)
- **Очистка сессий**: Автоматическое удаление просроченных сессий каждые 5 минут
- **Telegram уведомления**: HTML форматирование с эмодзи и кнопками
- **Часовой пояс**: Московское время (UTC+3) для отображения времени

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

#### Верификация кодов
- `verification.session.cleanup.minutes` - время жизни сессий верификации в минутах (по умолчанию: 30)

#### Telegram Bot
- `TELEGRAM_BOT_TOKEN` - токен Telegram бота (обязательно)
- `TELEGRAM_BOT_NAME` - имя Telegram бота (опционально)
- `TELEGRAM_BOT_USERNAME` - username Telegram бота (опционально)

#### Временные коды
- `TELEGRAM_CODE_EXPIRATION_MINUTES` - время действия кода в минутах (по умолчанию: 5)

#### Верификация кодов
- `verification.session.cleanup.minutes` - время жизни сессий верификации в минутах (по умолчанию: 30)

#### Приложение
- `SERVER_PORT` - порт приложения (по умолчанию: 8080)
- `SPRING_PROFILES_ACTIVE` - активный профиль Spring (dev/prod)

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
- **Kafka интеграция**: Добавлена поддержка верификации кодов через Kafka
- **Миграции БД**: Добавлена таблица verification_sessions для сессий верификации
- **Конфигурация Kafka**: Обновлена для поддержки JSON сериализации

## Мониторинг и логирование
- Логи приложения: `logs/application.log`
- Метрики Spring Boot Actuator
- Health checks для всех компонентов
- **StartupInfoService** - автоматическое логирование параметров подключений при старте приложения
- **Kafka мониторинг** - проверка состояния consumer groups и топиков
- **Мониторинг верификации** - отслеживание состояния сессий и обработки сообщений

### Мониторинг кодов
- **Метрики генерации**: Количество созданных кодов, время генерации
- **Метрики очистки**: Количество удаленных просроченных кодов
- **Алерты**: Уведомления при превышении лимита попыток генерации
- **Dashboard**: Статистика использования команды /code

### Мониторинг верификации
- **Метрики Kafka**: Количество обработанных сообщений, время обработки
- **Метрики сессий**: Количество активных, подтвержденных и отозванных сессий
- **Метрики Telegram**: Успешность отправки уведомлений, время ответа пользователей
- **Health checks**: Проверка доступности Kafka, состояния consumer groups
- **Алерты**: Уведомления при ошибках обработки, недоступности Kafka
- **Метрики callback'ов**: Статистика нажатий кнопок и времени ответа пользователей

### Уровни логирования по профилям
- **dev**: INFO для всех компонентов, включая Kafka и Hibernate
- **prod**: WARN для Kafka, Hibernate и Spring Framework, INFO для основного приложения

### Логирование верификации
- **Kafka операции**: INFO уровень с correlationId для отслеживания
- **Telegram уведомления**: INFO уровень с messageId и результатами
- **Сессии верификации**: INFO уровень с деталями создания и обновления
- **Ошибки обработки**: ERROR уровень с полным контекстом и correlationId
- **Callback операции**: INFO уровень с деталями нажатий кнопок

### Логирование операций с кодами
- Генерация новых кодов: INFO уровень с деталями (пользователь, код, время истечения)
- Поиск существующих кодов: INFO уровень
- Очистка просроченных кодов: INFO уровень с количеством удаленных записей
- Ошибки генерации: ERROR уровень с деталями

### Логирование операций верификации
- Запросы верификации: INFO уровень с correlationId, кодом и деталями браузера
- Создание сессий верификации: INFO уровень с деталями сессии
- Отправка уведомлений в Telegram: INFO уровень с messageId
- Подтверждение/отзыв верификации: INFO уровень с изменением статуса
- Очистка просроченных сессий: INFO уровень с количеством удаленных записей
- Ошибки Kafka: ERROR уровень с деталями и correlationId
- Обработка callback'ов: INFO уровень с результатами нажатий кнопок

### Технические детали реализации
- **Генерация кодов**: ThreadLocalRandom для криптографически безопасной генерации
- **Проверка уникальности**: Оптимизированные SQL запросы с индексами
- **Временные метки**: LocalDateTime с автоматическим определением часового пояса
- **Транзакции**: Spring Data JPA транзакции для атомарности операций
- **JSON обработка**: Jackson для сериализации/десериализации Kafka сообщений
- **UUID генерация**: SecureRandom для создания уникальных correlationId

### Технические детали верификации
- **UUID корреляция**: Уникальные correlationId для связывания запросов и ответов
- **JSON хранение**: Browser info сохраняется в JSON формате в PostgreSQL
- **Статусы сессий**: PENDING → CONFIRMED/REVOKED с автоматическим обновлением
- **Kafka сериализация**: Jackson JSON для DTO объектов
- **Manual acknowledgment**: Ручное подтверждение обработки Kafka сообщений
- **Graceful shutdown**: Корректное завершение обработки сообщений при остановке
- **Telegram API**: Асинхронная отправка уведомлений с inline клавиатурами
- **Callback обработка**: Асинхронная обработка нажатий кнопок с валидацией

### Информация при старте
При запуске приложения автоматически выводится:
- Активный профиль и порт сервера
- Параметры подключения к базе данных (маскированные)
- Настройки Kafka (серверы, протокол безопасности)
- Конфигурация Telegram Bot (username, токен маскирован)
- Состояние Kafka топиков верификации
- Статус готовности всех компонентов
- Количество активных сессий верификации
- Статистика обработки сообщений верификации
- Состояние consumer groups и топиков верификации

### Команды Telegram бота
- `/code` - генерация временного 7-значного кода для пользователя
  - Если у пользователя есть активный код - возвращает существующий
  - Если нет - генерирует новый уникальный код
  - Показывает время действия в часовом поясе МСК (UTC+3)
  - Формат ответа: 🔑 Ваш код: 1234567 ⏰ Действителен до: 15:30 (МСК)

### Callback'ы верификации
- **Подтверждение**: `confirm_{correlationId}` - подтверждение авторизации
- **Отзыв**: `revoke_{correlationId}` - отзыв авторизации
- **Обработка**: Автоматическое обновление сообщений и статусов сессий
- **Безопасность**: Валидация correlationId и проверка принадлежности сессии
- **Логирование**: Полная аудитория всех действий пользователей с сессиями
- **Обработка ошибок**: Graceful fallback при недоступности компонентов

## Безопасность
- Чувствительные данные в переменных окружения
- Валидация входящих данных
- Логирование действий пользователей
- Rate limiting для API endpoints
- UUID корреляция для предотвращения повторных запросов
- Валидация Kafka сообщений и DTO объектов
- Аудит всех операций верификации и отзыва авторизации
- Защита от CSRF атак через валидацию callback'ов

### Безопасность кодов
- **Уникальность**: Гарантированная уникальность каждого кода в системе
- **Временное ограничение**: Автоматическое истечение кодов
- **Изоляция**: Коды привязаны к конкретному пользователю
- **Логирование**: Полная аудитория всех операций с кодами

### Безопасность верификации
- **UUID корреляция**: Защита от повторных запросов с одним correlationId
- **Валидация кодов**: Проверка принадлежности кода пользователю и срока действия
- **Изоляция сессий**: Каждая сессия верификации независима
- **Логирование безопасности**: Аудит всех попыток верификации и отзыва
- **Rate limiting**: Ограничение частоты запросов верификации
- **Telegram callback валидация**: Проверка принадлежности сессии пользователю
- **Защита от replay атак**: Уникальность correlationId для каждой операции
- **Валидация browser info**: Проверка корректности IP и User-Agent данных

## Валидация и обработка ошибок

### Валидация кодов
- Проверка уникальности: Автоматическая генерация нового кода при конфликте
- Максимальное количество попыток: 100 для генерации уникального кода
- Валидация формата: Строго 7 цифр от 1000000 до 9999999

### Валидация верификации
- Проверка correlationId: Уникальность UUID для каждой сессии
- Валидация кода: Проверка существования и срока действия
- Проверка browser info: Валидация IP, User-Agent и локации
- Валидация статуса: Разрешенные переходы PENDING → CONFIRMED/REVOKED
- Валидация Kafka сообщений: Проверка структуры и обязательных полей
- Валидация callback'ов: Проверка формата и принадлежности сессий
- Валидация временных меток: Проверка корректности timestamp данных

### Обработка исключений
- Ошибки БД: Логирование с деталями, возврат пользователю понятного сообщения
- Ошибки генерации: Fallback на повторные попытки с ограничением
- Недоступность БД: Graceful degradation с логированием ошибок

### Обработка ошибок верификации
- Ошибки Kafka: Retry механизмы, логирование с correlationId
- Недоступность Telegram: Fallback на повторные попытки отправки
- Ошибки валидации: Детальные сообщения об ошибках в Kafka ответах
- Timeout обработки: Автоматическая очистка просроченных сессий
- Ошибки callback'ов: Валидация и безопасная обработка нажатий кнопок
- Ошибки сериализации: Fallback на базовые форматы сообщений
- Ошибки БД: Graceful degradation с сохранением состояния сессий

### Обработка часовых поясов
- По умолчанию: UTC+3 (Московское время)
- Fallback: При ошибке определения часового пояса пользователя
- Форматирование: Время в формате HH:mm с указанием часового пояса
- Верификация: Время создания сессии в московском часовом поясе
- Локализация: Поддержка разных форматов времени для разных регионов
- Синхронизация: Автоматическое обновление времени в сообщениях верификации


## CI/CD Pipeline
- Автоматическая сборка при изменении кода
- Сборка Docker образа
- Деплой на Ubuntu 20.04 сервер
- **Проверка Kafka**: Валидация подключения к Kafka серверам
- **Проверка миграций**: Валидация Liquibase changelog файлов
- **Проверка DTO**: Валидация структуры Kafka сообщений
- **Проверка callback'ов**: Валидация обработки Telegram callback'ов

## Производительность

### Оптимизация работы с кодами
- **Индексы БД**: Оптимизированные запросы по code, telegram_user_id, expires_at
- **Кэширование**: Проверка существующих кодов перед генерацией новых
- **Batch операции**: Массовое удаление просроченных кодов
- **Планировщик**: Оптимальная частота очистки (каждые 5 минут)

### Оптимизация верификации
- **Индексы БД**: Оптимизированные запросы по correlation_id, telegram_user_id, status+created_at
- **JSON индексы**: Быстрый поиск по browser info
- **Batch очистка**: Массовое удаление просроченных сессий
- **Kafka оптимизация**: Manual acknowledgment, idempotent producers
- **Telegram оптимизация**: Асинхронная отправка уведомлений
- **Кэширование сессий**: Быстрый доступ к активным сессиям верификации
- **Оптимизация JSON**: Эффективная обработка browser info данных
- **Оптимизация callback'ов**: Быстрая обработка нажатий кнопок

### Масштабируемость
- **Уникальность кодов**: Алгоритм с ограничением попыток для предотвращения бесконечных циклов
- **Очистка данных**: Автоматическое освобождение места в БД
- **Асинхронность**: Неблокирующая генерация кодов

### Масштабируемость верификации
- **Kafka партиции**: Распределение нагрузки по партициям топиков
- **Consumer группы**: Изоляция обработки разных типов сообщений
- **Сессии в БД**: Автоматическая очистка для предотвращения переполнения
- **Telegram API**: Асинхронная обработка callback'ов и уведомлений
- **Горизонтальное масштабирование**: Возможность запуска нескольких экземпляров приложения
- **Load balancing**: Распределение Kafka сообщений между экземплярами
- **Шардинг БД**: Возможность распределения сессий по разным базам данных
- **Масштабирование callback'ов**: Распределение обработки нажатий кнопок

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

### Верификация кодов через Kafka
```
1. Запрос верификации:
   POST /api/verification/verify
   {
     "code": "1234567",
     "ip": "192.168.1.1",
     "userAgent": "Chrome/120.0.0.0",
     "location": "Moscow, Russia"
   }

2. Сообщение в Telegram:
   🔐 Запрос авторизации
   
   IP: 192.168.1.1
   Браузер: Chrome/120.0.0.0
   Время: 15:30 (МСК)
   
   Подтвердите авторизацию:
   [✅ Подтвердить] [❌ Отозвать авторизацию]

3. При подтверждении:
   ✅ Авторизация подтверждена

4. При отзыве:
   ⏳ Отзываем авторизацию...
   ❌ Авторизация отозвана
```

### Логирование операций
```
2024-01-01 15:25:00 INFO  - Создан код для пользователя 123: 1234567, истекает в 2024-01-01T15:30:00
2024-01-01 15:25:05 INFO  - Найден существующий код для пользователя 123: 1234567
2024-01-01 15:30:00 INFO  - Удалено 1 просроченных кодов
```

### Логирование операций верификации
```
2024-01-01 15:25:00 INFO  - Запрос верификации: correlationId=uuid, code=1234567
2024-01-01 15:25:01 INFO  - Создана сессия верификации: correlationId=uuid, telegramUserId=123
2024-01-01 15:25:02 INFO  - Уведомление отправлено в Telegram: messageId=456
2024-01-01 15:25:10 INFO  - Верификация подтверждена: correlationId=uuid
2024-01-01 15:30:00 INFO  - Удалено 2 просроченных сессий верификации
```
