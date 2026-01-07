# Архитектура приложения naidizakupku_telegram

## Обзор
Telegram-бот для поиска и покупки товаров с интеграцией Kafka для асинхронной обработки сообщений.

## Технологический стек
- **Язык**: Kotlin 2.0
- **Фреймворк**: Spring Boot 3.5.0
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
- `UserController.kt` - REST API для управления пользователями (CRUD операции, уведомления)
- `CodeController.kt` - REST API для работы с кодами верификации
- `interceptor/TracingInterceptor.kt` - перехватчик для автоматической обработки заголовков трассировки
- `interceptor/RateLimitInterceptor.kt` - перехватчик для rate limiting API endpoints с использованием Bucket4j
- `exception/GlobalExceptionHandler.kt` - глобальный обработчик исключений для REST API с централизованной обработкой ошибок

### 2. Service Layer (`service/`)
**Назначение**: Бизнес-логика приложения
- `TelegramBotService.kt` - основной сервис Telegram бота с эхо-функцией, обработкой команды /code и callback'ов авторизации
- `UserService.kt` - управление пользователями
- `UserCodeService.kt` - управление временными кодами пользователей и обработка авторизации с Telegram уведомлениями
- `CodeGenerationService.kt` - генерация уникальных 7-значных кодов
- `KafkaService.kt` - отправка сообщений в Kafka топики
- `KafkaConsumerService.kt` - потребление сообщений из Kafka топиков
- `StartupInfoService.kt` - отображение параметров подключений и интеграций при старте приложения
- `DatabaseHealthService.kt` - проверка здоровья базы данных и проверка таблиц
- `KafkaVerificationService.kt` - основная логика верификации кодов через Kafka
- `VerificationSessionService.kt` - управление сессиями верификации
- `TelegramNotificationService.kt` - отправка уведомлений в Telegram для верификации и авторизации с inline кнопками
- `KafkaProducerService.kt` - отправка сообщений в Kafka топики верификации
- `MetricsService.kt` - сервис для кастомных метрик приложения (коды, верификация, Telegram операции)
- `TelegramBotExecutor.kt` - интерфейс для выполнения операций с Telegram Bot API (устранение циклических зависимостей)
- `AdminAuthService.kt` - сервис аутентификации для админки (JWT токены, refresh токены)
- `AdminAuditService.kt` - сервис аудита действий администраторов
- `admin/AdminService.kt` - основной сервис для работы с данными в админке (пользователи, коды, сессии верификации, запросы аутентификации)
- `admin/AdminMetricsService.kt` - сервис для агрегации метрик для админки с кэшированием (dashboard, коды, верификация, Telegram, Kafka)
- `admin/AdminLogService.kt` - сервис для работы с логами в админке
- `admin/KafkaAdminService.kt` - сервис для получения статуса Kafka (топики, consumer groups)
- `admin/AdminUserCreationService.kt` - сервис для создания первого администратора (доступен только если в системе нет администраторов)

### 3. Repository Layer (`repository/`)
**Назначение**: Доступ к данным
- `UserRepository.kt` - CRUD операции для пользователей
- `UserCodeRepository.kt` - CRUD операции для временных кодов пользователей
- `VerificationSessionRepository.kt` - CRUD операции для сессий верификации
- `AuthRequestRepository.kt` - CRUD операции для запросов аутентификации с методами поиска и удаления по traceId

### 4. Domain Layer (`domain/`)
**Назначение**: Модели данных и бизнес-сущности
- `User.kt` - сущность пользователя
- `UserCode.kt` - сущность временного кода пользователя
- `VerificationSession.kt` - сущность сессии верификации кода
- `VerificationStatus.kt` - enum статусов верификации (PENDING, CONFIRMED, REVOKED)
- `AuthRequest.kt` - сущность запроса аутентификации

### 5. Config Layer (`config/`)
**Назначение**: Конфигурация приложения
- `DatabaseConfig.kt` - конфигурация базы данных с условной загрузкой Liquibase
- `KafkaConfig.kt` - настройки Kafka, создание топиков (user-events, notifications, верификация)
- `TelegramConfig.kt` - настройки Telegram Bot (токен, имя, username)
- `TelegramBotProperties.kt` - data class для свойств конфигурации Telegram бота с префиксом `telegram.bot`, содержит token и name, используется в TelegramConfig для получения настроек бота из application.yml
- `RateLimitConfig.kt` - конфигурация rate limiting для API endpoints с использованием Bucket4j (общий API, верификация кодов, генерация кодов, админка)
- `WebClientConfig.kt` - конфигурация WebClient для HTTP запросов
- `CacheConfig.kt` - конфигурация кэширования для приложения, включая отдельный кэш для метрик админки (TTL: 2 минуты, максимальный размер: 100 записей)
- `WebMvcConfig.kt` - конфигурация для раздачи статических ресурсов админки через Spring Boot, поддержка SPA routing

### 6. Handler Layer (`handler/`)
**Назначение**: Обработчики команд Telegram бота
- `TelegramCodeHandler.kt` - обработчик команды /code для генерации временных кодов
- `VerificationCallbackHandler.kt` - обработчик callback'ов для кнопок верификации

### 7. Scheduler Layer (`scheduler/`)
**Назначение**: Планировщики задач
- `CodeCleanupScheduler.kt` - автоматическая очистка просроченных кодов каждые 5 минут (300000 мс)
- `VerificationSessionCleanupScheduler.kt` - автоматическая очистка просроченных сессий верификации каждые 5 минут

## REST API Endpoints

### UserController (`/api/users`)
**Назначение**: Управление пользователями и отправка уведомлений

#### Endpoints:
- `POST /api/users` - создание нового пользователя
  - **Request Body**: `User` объект
  - **Response**: `User` объект
  - **Kafka Event**: `user_registered`

- `GET /api/users/{id}` - получение пользователя по ID
  - **Path Parameter**: `id` (Long)
  - **Response**: `User` объект

- `GET /api/users` - получение всех пользователей
  - **Response**: `List<User>`

- `PUT /api/users/{id}` - обновление пользователя
  - **Path Parameter**: `id` (Long)
  - **Request Body**: `User` объект
  - **Response**: `User` объект
  - **Kafka Event**: `user_updated`

- `DELETE /api/users/{id}` - удаление пользователя
  - **Path Parameter**: `id` (Long)
  - **Response**: `200 OK`
  - **Kafka Event**: `user_deleted`

- `POST /api/users/{id}/notify` - отправка уведомления пользователю
  - **Path Parameter**: `id` (Long)
  - **Request Body**: `{"message": "текст", "type": "info"}`
  - **Response**: `{"status": "success", "message": "Уведомление отправлено в очередь"}`
  - **Kafka Event**: отправка в топик `notifications`

### CodeController (`/api/code`)
**Назначение**: Работа с кодами верификации

#### Endpoints:
- `POST /api/code/verify` - проверка кода
  - **Request Body**: `VerificationRequest`
    ```json
    {
      "code": "1234567",
      "ip": "192.168.1.1",
      "userAgent": "Chrome/120.0.0.0",
      "location": "Moscow, Russia"
    }
    ```
  - **Response**: `Boolean` (true если код валиден)
  - **Функциональность**: Проверяет существование и валидность кода через `UserCodeService.verifyCode()`

- `POST /api/code/auth` - проверка кода для аутентификации
  - **Request Body**: `VerificationRequest`
    ```json
    {
      "code": "1234567",
      "ip": "192.168.1.1",
      "userAgent": "Chrome/120.0.0.0",
      "location": "Moscow, Russia"
    }
    ```
  - **Headers**: `X-Trace-Id` (UUID) - уникальный идентификатор запроса
  - **Response**: `Boolean` (true если код валиден)
  - **Функциональность**: Проверяет код и сохраняет запрос аутентификации в БД через `UserCodeService.verifyCodeForAuth()`
  - **Сохранение**: Создает запись в таблице `auth_requests` с trace_id и telegram_user_id для отслеживания запросов
  - **Telegram уведомление**: Отправляет пользователю сообщение с кнопками подтверждения/отклонения входа

- `GET /api/code/status/{correlationId}` - получение статуса сессии верификации
  - **Path Parameter**: `correlationId` (UUID)
  - **Response**: 
    ```json
    {
      "correlationId": "uuid",
      "status": "PENDING|CONFIRMED|REVOKED",
      "message": "описание статуса"
    }
    ```
  - **Статус**: TODO - требует реализации получения статуса из сервиса
  - **Валидация**: Проверка корректности UUID формата

### AdminController (`/api/admin`)
**Назначение**: REST API для административной панели

#### Endpoints:

##### Users Management
- `GET /api/admin/users` - получение списка пользователей с пагинацией, поиском и фильтрами
  - **Query Parameters**: `page` (default: 0), `size` (default: 20), `search` (optional), `active` (optional)
  - **Response**: `PagedResponse<UserDto>`
  
- `GET /api/admin/users/{id}` - получение пользователя по ID
  - **Path Parameter**: `id` (Long)
  - **Response**: `UserDto`
  
- `PUT /api/admin/users/{id}/activate` - активация пользователя
  - **Path Parameter**: `id` (Long)
  - **Response**: `UserDto`
  
- `PUT /api/admin/users/{id}/deactivate` - деактивация пользователя
  - **Path Parameter**: `id` (Long)
  - **Response**: `UserDto`

##### Codes Management
- `GET /api/admin/codes` - получение списка кодов с пагинацией и фильтрами
  - **Query Parameters**: `page`, `size`, `userId` (optional), `active` (optional), `expired` (optional)
  - **Response**: `PagedResponse<CodeDto>`
  
- `GET /api/admin/codes/{id}` - получение кода по ID
  - **Path Parameter**: `id` (Long)
  - **Response**: `CodeDto`
  
- `DELETE /api/admin/codes/{id}` - удаление кода
  - **Path Parameter**: `id` (Long)
  - **Response**: `{"message": "Code deleted successfully"}`
  
- `GET /api/admin/codes/stats` - статистика по кодам
  - **Response**: `CodeStatsDto` (total, active, expired)

##### Verification Sessions
- `GET /api/admin/verification-sessions` - получение списка сессий верификации
  - **Query Parameters**: `page`, `size`, `status` (optional), `userId` (optional)
  - **Response**: `PagedResponse<AdminVerificationDto>`
  
- `GET /api/admin/verification-sessions/{correlationId}` - получение сессии по correlation ID
  - **Path Parameter**: `correlationId` (UUID)
  - **Response**: `AdminVerificationDto`
  
- `GET /api/admin/verification-sessions/stats` - статистика по сессиям верификации
  - **Response**: `VerificationStatsDto`

##### Auth Requests
- `GET /api/admin/auth-requests` - получение списка запросов аутентификации
  - **Query Parameters**: `page`, `size`, `userId` (optional), `traceId` (optional)
  - **Response**: `PagedResponse<AuthRequestDto>`
  
- `GET /api/admin/auth-requests/{traceId}` - получение запроса по trace ID
  - **Path Parameter**: `traceId` (UUID)
  - **Response**: `AuthRequestDto`
  
- `GET /api/admin/auth-requests/stats` - статистика по запросам аутентификации
  - **Response**: `AuthRequestStatsDto`

##### Metrics
- `GET /api/admin/metrics/dashboard` - агрегированные метрики для dashboard
  - **Response**: `MetricsDto` (codes, verification, telegram, kafka)
  - **Кэширование**: 2 минуты
  
- `GET /api/admin/metrics/codes` - метрики кодов
  - **Query Parameter**: `period` (24h, 7d, 30d, default: 24h)
  - **Response**: `CodeMetrics`
  - **Кэширование**: 2 минуты
  
- `GET /api/admin/metrics/verification` - метрики верификации
  - **Query Parameter**: `period` (24h, 7d, 30d, default: 24h)
  - **Response**: `VerificationMetrics`
  - **Кэширование**: 2 минуты
  
- `GET /api/admin/metrics/telegram` - метрики Telegram
  - **Query Parameter**: `period` (24h, 7d, 30d, default: 24h)
  - **Response**: `TelegramMetrics`
  - **Кэширование**: 2 минуты
  
- `GET /api/admin/metrics/kafka` - метрики Kafka
  - **Query Parameter**: `period` (24h, 7d, 30d, default: 24h)
  - **Response**: `KafkaMetrics`
  - **Кэширование**: 2 минуты

##### Kafka
- `GET /api/admin/kafka/topics` - статус Kafka топиков
  - **Response**: `KafkaStatusDto` (список топиков с информацией о партициях, репликах, размере)
  
- `GET /api/admin/kafka/consumer-groups` - список consumer groups
  - **Response**: `List<ConsumerGroupInfo>`

##### Logs
- `GET /api/admin/logs` - получение логов
  - **Query Parameters**: `level` (optional), `limit` (default: 100)
  - **Response**: `List<LogEntry>`

##### Settings
- `GET /api/admin/settings` - получение настроек
  - **Response**: `SettingsDto`
  
- `PUT /api/admin/settings` - обновление настроек
  - **Request Body**: `SettingsDto`
  - **Response**: `SettingsDto`

### AuthController (`/api/admin/auth`)
**Назначение**: Аутентификация администраторов

#### Endpoints:
- `POST /api/admin/auth/login` - вход в систему
  - **Request Body**: `{"username": "string", "password": "string"}`
  - **Response**: `AuthResponse` (accessToken, refreshToken, expiresAt, user)
  - **Аудит**: Логирование попыток входа (успешных и неудачных)
  
- `POST /api/admin/auth/refresh` - обновление токена
  - **Request Body**: `{"refreshToken": "string"}`
  - **Response**: `AuthResponse` (новые accessToken и refreshToken)
  
- `POST /api/admin/auth/logout` - выход из системы
  - **Headers**: `Authorization: Bearer <token>`
  - **Response**: `{"message": "Successfully logged out"}`
  - **Функциональность**: Удаление сессии из БД
  
- `GET /api/admin/auth/me` - информация о текущем пользователе
  - **Headers**: `Authorization: Bearer <token>`
  - **Response**: `CurrentUserResponse` (id, username, email, role)
  
- `POST /api/admin/auth/register-first-admin` - регистрация первого администратора
  - **Request Body**: `{"username": "string", "password": "string", "email": "string (optional)"}`
  - **Response**: `CurrentUserResponse` (id, username, email, role)
  - **Доступность**: Доступен только если в системе нет администраторов
  - **Функциональность**: Создает первого администратора с ролью ADMIN через `AdminUserCreationService`
  
- `GET /api/admin/auth/has-admins` - проверка наличия администраторов в системе
  - **Response**: `{"hasAdmins": boolean}`
  - **Функциональность**: Проверяет наличие администраторов в системе

### TracingInterceptor
**Назначение**: Автоматическая обработка заголовков трассировки

#### Поддерживаемые заголовки:
- `X-Trace-Id` - уникальный идентификатор всего запроса
- `X-Span-Id` - идентификатор текущего участка обработки
- `X-Request-Id` - общий идентификатор запроса пользователя
- `X-Correlation-Id` - для корреляции связанных запросов
- `X-Parent-Span-Id` - идентификатор родительского span'а

#### Функциональность:
- Автоматическая генерация trace/span ID если не переданы
- Добавление заголовков в MDC для логирования
- Возврат заголовков в response для клиента
- Сохранение контекста в request attributes

## Административная панель

### Обзор
Административная панель предоставляет веб-интерфейс для управления системой, мониторинга метрик и аудита действий. Панель использует JWT аутентификацию с refresh токенами и разделением ролей.

### Раздача статических ресурсов
Админка интегрирована в Spring Boot приложение и раздается как статические файлы:
- **Путь**: `/admin/**` - все запросы к админке
- **Статические файлы**: `/assets/**` - JS, CSS и другие ресурсы
- **SPA Routing**: Все маршруты без расширения файла возвращают `index.html` для поддержки client-side routing
- **Кеширование**: HTML файлы кешируются на 1 час, assets на 24 часа
- **Конфигурация**: `WebMvcConfig.kt` - настройка resource handlers и SPA routing

### Архитектура админки

#### Frontend (React + TypeScript)
- **Фреймворк**: React 18 с TypeScript
- **UI библиотека**: Ant Design 5
- **Роутинг**: React Router v6
- **State Management**: Zustand
- **HTTP клиент**: Axios
- **Запросы**: React Query (TanStack Query)
- **Графики**: Recharts
- **Оптимизации**: 
  - Lazy loading страниц
  - Виртуализация больших списков
  - Кэширование метрик (2 минуты)
  - Code splitting для уменьшения bundle size
  - React.memo и useMemo для оптимизации ререндеров

#### Backend (Spring Boot)
- **Контроллеры**: 
  - `AdminController` - основной API для работы с данными
  - `AuthController` - аутентификация и авторизация
- **Сервисы**:
  - `AdminService` - бизнес-логика работы с данными
  - `AdminAuthService` - аутентификация и управление сессиями
  - `AdminMetricsService` - агрегация метрик с кэшированием
  - `AdminLogService` - работа с логами
  - `AdminAuditService` - аудит действий администраторов
  - `KafkaAdminService` - мониторинг Kafka
- **Безопасность**:
  - JWT токены (access + refresh)
  - Spring Security с кастомным фильтром
  - Rate limiting (60 запросов/минуту на IP)
  - Аудит всех действий
  - Разрешен доступ к статическим ресурсам `/admin/**` и `/assets/**` без аутентификации
  - CORS настроен для работы с админкой (поддержка credentials, настраиваемые origins)

### Процесс аутентификации

#### 1. Вход в систему (`POST /api/admin/auth/login`)

**Шаги**:
1. Клиент отправляет `username` и `password` в теле запроса
2. `AdminAuthService.login()` получает запрос
3. Spring Security `AuthenticationManager` проверяет учетные данные:
   - Загружает пользователя через `AdminUserDetailsService`
   - Проверяет пароль через `PasswordEncoder` (BCrypt)
   - Проверяет, что пользователь активен (`active = true`)
4. При успешной аутентификации:
   - Генерируется JWT access token (срок действия: настраивается, по умолчанию 60 минут)
   - Генерируется JWT refresh token (срок действия: настраивается, по умолчанию 7 дней)
   - Создается запись в таблице `admin_sessions` с токенами и временем истечения
   - Логируется успешный вход в `admin_audit_log`
   - Возвращается `AuthResponse` с токенами и информацией о пользователе

**Обработка ошибок**:
- Неверные учетные данные → HTTP 401, логирование в audit log
- Неактивный пользователь → HTTP 401
- Валидация входных данных → HTTP 400

#### 2. Использование access token

**Защищенные endpoints**:
- Все endpoints `/api/admin/*` (кроме `/api/admin/auth/login` и `/api/admin/auth/refresh`)
- Требуют заголовок `Authorization: Bearer <access_token>`

**Процесс**:
1. Клиент добавляет заголовок `Authorization: Bearer <access_token>` к каждому запросу
2. `JwtAuthenticationFilter` перехватывает запрос:
   - Извлекает токен из заголовка
   - Валидирует токен через `JwtService.validateToken()`
   - Проверяет, что пользователь существует и активен
   - Создает `Authentication` объект для Spring Security
3. При успешной валидации запрос проходит дальше
4. При ошибке → HTTP 401 Unauthorized

#### 3. Обновление токена (`POST /api/admin/auth/refresh`)

**Шаги**:
1. Клиент отправляет `refreshToken` в теле запроса
2. `AdminAuthService.refreshToken()`:
   - Находит сессию по `refreshToken` в БД
   - Проверяет, что сессия не истекла
   - Проверяет, что пользователь активен
   - Генерирует новые access и refresh токены
   - Обновляет запись в `admin_sessions`
   - Удаляет старую сессию
   - Возвращает новые токены

**Обработка ошибок**:
- Неверный или истекший refresh token → HTTP 401
- Неактивный пользователь → HTTP 401

#### 4. Выход из системы (`POST /api/admin/auth/logout`)

**Шаги**:
1. Клиент отправляет запрос с заголовком `Authorization: Bearer <access_token>`
2. `AdminAuthService.logout()`:
   - Извлекает токен из заголовка
   - Находит сессию по access token
   - Удаляет сессию из БД (`admin_sessions`)
   - Логирует выход в `admin_audit_log`
3. Возвращается успешный ответ

#### 5. Получение информации о текущем пользователе (`GET /api/admin/auth/me`)

**Шаги**:
1. Клиент отправляет запрос с заголовком `Authorization: Bearer <access_token>`
2. `AuthController.getCurrentUser()`:
   - Валидирует токен через `AdminAuthService.validateToken()`
   - Возвращает информацию о пользователе (id, username, email, role)

### Роли администраторов

- **ADMIN** - полный доступ ко всем функциям админки
- **VIEWER** - только просмотр данных, без возможности изменений

### Аудит действий

Все действия администраторов логируются в таблицу `admin_audit_log`:
- **LOGIN** / **LOGIN_FAILED** - попытки входа
- **LOGOUT** - выход из системы
- **USER_ACTIVATED** / **USER_DEACTIVATED** - активация/деактивация пользователей
- **CODE_DELETED** - удаление кодов
- И другие действия в зависимости от операций

Каждая запись содержит:
- ID администратора (или NULL для системных действий)
- Тип действия
- Тип и ID затронутой сущности
- Детали действия
- IP адрес и User-Agent
- Время действия

### Кэширование метрик

Метрики админки кэшируются для снижения нагрузки:
- **Провайдер**: Caffeine
- **TTL**: 2 минуты
- **Максимальный размер**: 100 записей
- **Кэшируемые методы**:
  - `getDashboardMetrics()` → кэш `dashboardMetrics`
  - `getCodeMetrics()` → кэш `codeMetrics`
  - `getVerificationMetrics()` → кэш `verificationMetrics`
  - `getTelegramMetrics()` → кэш `telegramMetrics`
  - `getKafkaMetrics()` → кэш `kafkaMetrics`

### Rate Limiting для админки

- **Лимит**: 60 запросов в минуту на IP адрес
- **Реализация**: Per-IP rate limiting через `ConcurrentHashMap` с отдельными buckets
- **Определение IP**: Поддержка заголовков `X-Forwarded-For` и `X-Real-IP`
- **Ответ при превышении**: HTTP 429 Too Many Requests

## Поток данных

### Основной поток
```
Telegram Bot API → TelegramBotService → UserService → UserRepository → PostgreSQL
                                    ↓
                              KafkaService → Kafka → KafkaConsumerService

REST API поток:
HTTP Client → UserController/CodeController → Service Layer → Repository Layer → PostgreSQL
                                    ↓
                              KafkaService → Kafka → KafkaConsumerService

Команда /code:
Telegram Bot API → TelegramBotService → TelegramCodeHandler → UserCodeService → CodeGenerationService → UserCodeRepository → PostgreSQL

Верификация кодов:
Kafka → VerificationRequestListener → KafkaVerificationService → VerificationSessionService → TelegramNotificationService → Telegram Bot API

REST API верификация:
HTTP Client → CodeController → UserCodeService → UserCodeRepository → PostgreSQL

Авторизация с Telegram подтверждением:
HTTP Client → CodeController → UserCodeService → TelegramNotificationService → Telegram Bot API
                                                      ↓
Telegram Bot API → TelegramBotService (callback) → UserCodeService → AuthRequestRepository → PostgreSQL
                                                      ↓
                                              KafkaProducerService → Kafka (при отзыве)
```

### Kafka потоки
- **Отправка событий**: `KafkaService.sendUserEvent()` → `user-events` топик
- **Отправка уведомлений**: `KafkaService.sendNotification()` → `notifications` топик
- **Потребление событий**: `user-events` топик → `KafkaConsumerService.handleUserEvent()`
- **Потребление уведомлений**: `notifications` топик → `KafkaConsumerService.handleNotification()`

### Kafka потоки верификации

## Новые методы для авторизации с Telegram подтверждением

### UserCodeService
- `verifyCodeForAuth(request: VerificationRequest, traceId: UUID)` - проверяет код для авторизации, сохраняет запрос в БД и отправляет уведомление в Telegram
- `confirmAuth(traceId: UUID)` - подтверждает вход пользователя, удаляет запрос из БД и убирает кнопки из Telegram
- `revokeAuth(traceId: UUID)` - отклоняет вход пользователя, отправляет сообщение в Kafka и уведомляет пользователя

### TelegramNotificationService
- `sendAuthConfirmationRequest(telegramUserId, traceId, ip, userAgent, location)` - отправляет уведомление о запросе авторизации с inline кнопками
- `removeAuthConfirmationButtons(telegramUserId, traceId)` - удаляет кнопки из сообщения подтверждения
- `sendAuthRevokedMessage(telegramUserId)` - отправляет сообщение об отзыве авторизации

### TelegramBotService
- `handleCallbackQuery(update: Update)` - обрабатывает callback'и от inline кнопок авторизации
- `answerCallbackQuery(callbackQueryId, text)` - отправляет ответ на callback query

### AuthRequestRepository
- `findByTraceId(traceId: UUID)` - находит запрос по trace ID
- `deleteByTraceId(traceId: UUID)` - удаляет запрос по trace ID
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

### Таблица `auth_requests`
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT) - уникальный идентификатор
- `trace_id` (UUID, UNIQUE, NOT NULL) - уникальный идентификатор запроса (UUID из header X-Trace-Id)
- `telegram_user_id` (BIGINT, NOT NULL) - ID пользователя Telegram
- `requested_at` (TIMESTAMP, NOT NULL) - время создания запроса
- `code` (VARCHAR(10), NULLABLE) - выданный код для аутентификации

### Таблица `admin_users`
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT) - уникальный идентификатор
- `username` (VARCHAR(100), UNIQUE, NOT NULL) - имя пользователя для входа
- `password_hash` (VARCHAR(255), NOT NULL) - хеш пароля (BCrypt)
- `email` (VARCHAR(255), UNIQUE, NULLABLE) - email адрес администратора
- `role` (VARCHAR(20), NOT NULL, DEFAULT 'VIEWER') - роль администратора (ADMIN, VIEWER)
- `active` (BOOLEAN, NOT NULL, DEFAULT TRUE) - статус активности пользователя
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP) - время создания записи
- `updated_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP) - время последнего обновления

### Таблица `admin_sessions`
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT) - уникальный идентификатор
- `admin_user_id` (BIGINT, NOT NULL) - ID администратора (FK к admin_users)
- `access_token` (TEXT, NOT NULL) - JWT access token
- `refresh_token` (TEXT, NOT NULL) - JWT refresh token
- `expires_at` (TIMESTAMP, NOT NULL) - время истечения access token
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP) - время создания сессии

### Таблица `admin_audit_log`
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT) - уникальный идентификатор
- `admin_user_id` (BIGINT, NULLABLE) - ID администратора (NULL для системных действий)
- `action` (VARCHAR(50), NOT NULL) - тип действия (LOGIN, LOGOUT, USER_ACTIVATED, и т.д.)
- `entity_type` (VARCHAR(50), NULLABLE) - тип затронутой сущности (USER, CODE, и т.д.)
- `entity_id` (BIGINT, NULLABLE) - ID затронутой сущности
- `details` (TEXT, NULLABLE) - детали действия (JSON или текст)
- `ip_address` (VARCHAR(45), NULLABLE) - IP адрес запроса
- `user_agent` (VARCHAR(500), NULLABLE) - User-Agent браузера
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP) - время действия

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

#### Индексы для таблицы `auth_requests`
- `idx_auth_requests_trace_id` - уникальный индекс по trace_id для быстрого поиска
- `idx_auth_requests_telegram_user_id` - индекс по telegram_user_id для поиска запросов пользователя
- `idx_auth_requests_requested_at` - индекс по requested_at для очистки старых запросов
- `idx_auth_requests_code` - индекс по code для быстрого поиска по коду

#### Индексы для таблицы `admin_users`
- `idx_admin_users_username` - уникальный индекс по username для быстрого поиска
- `idx_admin_users_email` - уникальный индекс по email (если указан)
- `idx_admin_users_role` - индекс по role для фильтрации по ролям
- `idx_admin_users_active` - индекс по active для фильтрации активных пользователей

#### Индексы для таблицы `admin_sessions`
- `idx_admin_sessions_admin_user_id` - индекс по admin_user_id для поиска сессий пользователя
- `idx_admin_sessions_refresh_token` - индекс по refresh_token для поиска сессии по токену
- `idx_admin_sessions_expires_at` - индекс по expires_at для очистки истекших сессий

#### Индексы для таблицы `admin_audit_log`
- `idx_admin_audit_log_admin_user_id` - индекс по admin_user_id для поиска действий администратора
- `idx_admin_audit_log_action` - индекс по action для фильтрации по типам действий
- `idx_admin_audit_log_created_at` - индекс по created_at для временных запросов
- `idx_admin_audit_log_entity` - составной индекс по entity_type и entity_id для поиска действий с сущностями

### Миграции
- **001-create-users-table.xml** - создание таблицы users
- **002-create-user-codes-table.xml** - создание таблицы user_codes для временных кодов
- **003-create-verification-sessions-table.xml** - создание таблицы verification_sessions для сессий верификации
- **004-create-auth-requests-table.xml** - создание таблицы auth_requests для запросов аутентификации
- **005-add-code-to-auth-requests.xml** - добавление столбца code в таблицу auth_requests
- **006-create-admin-users-table.xml** - создание таблицы admin_users для пользователей админки
- **007-create-admin-sessions-table.xml** - создание таблицы admin_sessions для JWT сессий
- **008-create-admin-audit-log-table.xml** - создание таблицы admin_audit_log для аудита действий администраторов
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

### Конфигурация раздачи статических ресурсов
- **WebMvcConfig**: Настройка resource handlers для админки
  - `/admin/**` → `classpath:/static/admin/` (кеширование 1 час)
  - `/assets/**` → `classpath:/static/admin/assets/` (кеширование 24 часа)
  - SPA routing: все запросы без расширения файла возвращают `index.html`
- **SecurityConfig**: Разрешен доступ к статическим ресурсам без аутентификации
- **CORS**: Настроен для работы с админкой (поддержка credentials, настраиваемые origins, max age 1 час)

### Конфигурация базы данных
- **PostgreSQL**: Основная БД для продакшна и разработки
- **Liquibase**: Управление миграциями БД с условной загрузкой
- **JPA/Hibernate**: ORM с автоматическим определением диалекта
- **Подключение**: Через переменные окружения с fallback значениями
- **DatabaseConfig**: Условная загрузка Liquibase только при наличии `spring.datasource.url`

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

### Конфигурация WebClient
- **WebClientConfig**: Конфигурация для HTTP запросов
- **Максимальный размер буфера**: 1MB для обработки больших ответов
- **Использование**: Для внешних HTTP вызовов в сервисах

### Конфигурация кэширования
- **Провайдер**: Caffeine (in-memory cache)
- **Общая конфигурация**: TTL 5 минут, максимальный размер 1000 записей
- **Кэш метрик админки**: Отдельный CacheManager (`metricsCacheManager`) для метрик
  - **TTL**: 2 минуты (метрики должны обновляться достаточно часто)
  - **Максимальный размер**: 100 записей
  - **Кэшируемые методы**: 
    - `AdminMetricsService.getDashboardMetrics()` - кэш `dashboardMetrics`
    - `AdminMetricsService.getCodeMetrics()` - кэш `codeMetrics`
    - `AdminMetricsService.getVerificationMetrics()` - кэш `verificationMetrics`
    - `AdminMetricsService.getTelegramMetrics()` - кэш `telegramMetrics`
    - `AdminMetricsService.getKafkaMetrics()` - кэш `kafkaMetrics`
- **Включение**: `@EnableCaching` в `TelegramApplication` и `CacheConfig`
- **Конфигурация**: `CacheConfig.kt` - программная настройка кэша для метрик

### Конфигурация Rate Limiting
- **Библиотека**: Bucket4j
- **Реализация**: `RateLimitInterceptor` - перехватчик для всех API endpoints
- **Типы rate limiters**:
  - **Общий API rate limiter**: 100 запросов в минуту на IP (для всех endpoints кроме специальных)
  - **Code verification rate limiter**: 10 запросов в минуту на IP (для `/api/code/` endpoints)
  - **Code generation rate limiter**: 5 запросов в минуту на пользователя (для генерации кодов через Telegram)
  - **Admin rate limiter**: 60 запросов в минуту на IP (для `/api/admin/` endpoints, per-IP rate limiting)
- **Per-IP rate limiting для админки**: Используется `ConcurrentHashMap` для хранения отдельных buckets для каждого IP адреса
- **Определение IP**: Поддержка заголовков `X-Forwarded-For` и `X-Real-IP` для работы за прокси/балансировщиком
- **Ответ при превышении лимита**: HTTP 429 (Too Many Requests) с JSON сообщением и `retryAfter: 60`
- **Логирование**: Все превышения лимитов логируются с уровнем WARN

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

#### Админка (Frontend)
- `VITE_API_URL` - URL API для админки (опционально, по умолчанию используется относительный путь `/api` в production)

#### Админка (Backend)
- `ADMIN_JWT_SECRET` - секретный ключ для JWT токенов (обязательно в production!)
- `ADMIN_JWT_ACCESS_EXPIRATION_MINUTES` - время жизни access token (по умолчанию: 60 минут)
- `ADMIN_JWT_REFRESH_EXPIRATION_DAYS` - время жизни refresh token (по умолчанию: 7 дней)

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

### Сборка админки

#### Локальная сборка
```bash
# Сборка админки отдельно
cd admin-panel
npm install
npm run build

# Сборка всего приложения (админка будет автоматически скопирована)
cd ..
./gradlew clean build
```

#### Docker сборка
Dockerfile использует multi-stage build:
1. **Stage 1 (frontend-build)**: Собирает админку через Node.js
2. **Stage 2 (build)**: Копирует собранную админку и собирает Spring Boot приложение
3. **Stage 3 (runtime)**: Создает runtime образ с JAR файлом

```bash
docker build -t naidizakupku-telegram .
```

#### Создание первого администратора

**Через API:**
```bash
# Проверка наличия администраторов
curl http://localhost:8080/api/admin/auth/has-admins

# Создание первого администратора
curl -X POST http://localhost:8080/api/admin/auth/register-first-admin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "securepassword123",
    "email": "admin@example.com"
  }'
```

**Через скрипт:**
```bash
# Linux/Mac
./scripts/create-first-admin.sh admin securepassword123 admin@example.com

# Windows
scripts\create-first-admin.bat admin securepassword123 admin@example.com
```

**Важно**: Endpoint `/api/admin/auth/register-first-admin` доступен только если в системе нет администраторов. После создания первого администратора этот endpoint становится недоступным.

#### Доступ к админке
- **Локально**: `http://localhost:8080/admin`
- **Production**: `https://your-domain.com/admin`

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

### Логирование REST API
- **TracingInterceptor**: Автоматическое добавление trace/span ID в MDC
- **HTTP запросы**: Логирование с traceId, spanId, requestId
- **API endpoints**: INFO уровень с деталями запросов и ответов
- **Ошибки API**: ERROR уровень с полным контекстом трассировки
- **Корреляция**: Связывание связанных запросов через correlationId

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

### Технические детали REST API
- **Spring WebMVC**: REST контроллеры с аннотациями @RestController
- **Suspend функции**: Асинхронная обработка запросов с корутинами (UserController)
- **Синхронные функции**: Обычные функции для CodeController (verifyCode, getVerificationStatus)
- **TracingInterceptor**: Автоматическая обработка заголовков трассировки
- **WebClient**: Реактивный HTTP клиент для внешних вызовов
- **ResponseEntity**: Стандартизированные HTTP ответы
- **Path Variables**: Валидация параметров пути
- **Request Body**: JSON десериализация в DTO объекты
- **Error Handling**: Централизованная обработка ошибок API
- **Data Classes**: VerificationRequest как data class с опциональными полями
- **UUID Parsing**: Try-catch блоки для валидации UUID формата

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
- `/help` или `/start` - отображение справки с доступными командами
  - Показывает список всех доступных команд бота
  - Формат ответа: HTML сообщение со списком команд и их описанием

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

### Безопасность REST API
- **TracingInterceptor**: Автоматическая генерация и валидация заголовков трассировки
- **Path Variables**: Валидация UUID и числовых параметров
- **Request Body**: Валидация JSON структуры и обязательных полей
- **CORS**: Настройка Cross-Origin Resource Sharing для веб-клиентов
- **Rate Limiting**: Ограничение частоты запросов к API endpoints
- **Input Sanitization**: Очистка пользовательского ввода от потенциально опасных данных
- **Error Handling**: Безопасное возвращение ошибок без утечки внутренней информации

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

### Валидация REST API
- **Path Variables**: Валидация UUID формата для correlationId, числовых ID
- **Request Body**: Валидация JSON структуры, обязательных полей
- **VerificationRequest**: Проверка формата кода (7 цифр), IP адреса, User-Agent (опциональные поля)
- **User объекты**: Валидация telegramId, имен, username
- **Notification объекты**: Проверка message и type полей (с fallback значениями)
- **HTTP Headers**: Валидация заголовков трассировки (X-Trace-Id, X-Span-Id)
- **Response Format**: Стандартизированные JSON ответы с корректными HTTP статусами
- **UUID валидация**: Проверка корректности correlationId с возвратом 400 Bad Request при ошибке

### Обработка исключений
- `CodeServiceException` - исключение для ошибок сервиса работы с кодами
- Ошибки БД: Логирование с деталями, возврат пользователю понятного сообщения
- Ошибки генерации: Fallback на повторные попытки с ограничением
- Недоступность БД: Graceful degradation с логированием ошибок
- `GlobalExceptionHandler` - централизованная обработка всех исключений REST API

### Обработка ошибок верификации
- Ошибки Kafka: Retry механизмы, логирование с correlationId
- Недоступность Telegram: Fallback на повторные попытки отправки
- Ошибки валидации: Детальные сообщения об ошибках в Kafka ответах
- Timeout обработки: Автоматическая очистка просроченных сессий
- Ошибки callback'ов: Валидация и безопасная обработка нажатий кнопок
- Ошибки сериализации: Fallback на базовые форматы сообщений
- Ошибки БД: Graceful degradation с сохранением состояния сессий

### Обработка ошибок REST API
- **HTTP Status Codes**: Корректные коды ответов (200, 400, 404, 500)
- **Validation Errors**: 400 Bad Request с деталями ошибок валидации
- **UUID Format Errors**: 400 Bad Request для некорректных correlationId
- **Not Found**: 404 для несуществующих ресурсов
- **Server Errors**: 500 с логированием и без утечки внутренней информации
- **Tracing**: Все ошибки логируются с traceId для корреляции
- **Graceful Degradation**: Fallback ответы при недоступности зависимостей
- **Error Response Format**: Стандартизированные JSON ответы с описанием ошибок
- **Fallback Values**: Дефолтные значения для опциональных полей (message, type, ip, userAgent)

### Обработка часовых поясов
- По умолчанию: UTC+3 (Московское время)
- Fallback: При ошибке определения часового пояса пользователя
- Форматирование: Время в формате HH:mm с указанием часового пояса
- Верификация: Время создания сессии в московском часовом поясе
- Локализация: Поддержка разных форматов времени для разных регионов
- Синхронизация: Автоматическое обновление времени в сообщениях верификации


## CI/CD Pipeline
- Автоматическая сборка при изменении кода
- **Сборка админки**: Отдельный job для сборки frontend (Node.js 20, npm ci, npm run build)
- **Интеграция админки**: Автоматическое копирование собранных файлов в `src/main/resources/static/admin/`
- Сборка Docker образа (multi-stage build с отдельным этапом для админки)
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

### Оптимизация REST API
- **Suspend функции**: Неблокирующая обработка HTTP запросов
- **WebClient**: Реактивные HTTP вызовы для внешних сервисов
- **TracingInterceptor**: Минимальные накладные расходы на трассировку
- **ResponseEntity**: Эффективная сериализация JSON ответов
- **Path Variables**: Быстрая валидация параметров пути
- **Request Body**: Оптимизированная десериализация JSON
- **Error Handling**: Быстрая обработка ошибок без лишних вычислений

### Масштабируемость REST API
- **Горизонтальное масштабирование**: Stateless REST endpoints
- **Load Balancing**: Распределение HTTP запросов между экземплярами
- **Connection Pooling**: Переиспользование HTTP соединений
- **Async Processing**: Асинхронная обработка с корутинами
- **Caching**: Возможность кэширования часто запрашиваемых данных
- **Rate Limiting**: Контроль нагрузки на API endpoints

### Исправления деплоя
- **Проблема с форматом переменных окружения**: Исправлены пробелы вокруг `=` в команде `docker run`
- **Добавлены переменные Kafka**: `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_USER`, `KAFKA_PASSWORD`
- **Проверка переменных окружения**: Добавлена валидация всех необходимых переменных перед деплоем
- **Улучшенная обработка ошибок**: Более детальные сообщения об ошибках и логирование

## Примеры использования

### REST API - Управление пользователями

#### Создание пользователя
```bash
POST /api/users
Content-Type: application/json

{
  "telegramId": 123456789,
  "firstName": "Иван",
  "lastName": "Петров",
  "username": "ivan_petrov"
}

Response: 200 OK
{
  "id": 1,
  "telegramId": 123456789,
  "firstName": "Иван",
  "lastName": "Петров",
  "username": "ivan_petrov",
  "createdAt": "2024-01-01T15:30:00",
  "active": true
}
```

#### Отправка уведомления
```bash
POST /api/users/1/notify
Content-Type: application/json

{
  "message": "Добро пожаловать!",
  "type": "info"
}

Response: 200 OK
{
  "status": "success",
  "message": "Уведомление отправлено в очередь"
}
```

### REST API - Верификация кодов

#### Проверка кода
```bash
POST /api/code/verify
Content-Type: application/json

{
  "code": "1234567",
  "ip": "192.168.1.1",
  "userAgent": "Chrome/120.0.0.0",
  "location": "Moscow, Russia"
}

Response: 200 OK
true
```

#### Получение статуса верификации
```bash
GET /api/code/status/550e8400-e29b-41d4-a716-446655440000

Response: 200 OK
{
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "message": "Status retrieval not implemented yet"
}

# При некорректном UUID:
Response: 400 Bad Request
{
  "error": "Invalid correlation ID format"
}
```

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
