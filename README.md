# Telegram Bot Application

Spring Boot приложение для Telegram бота на Kotlin.

## Технологии

- **Kotlin 2.0**
- **Spring Boot 3.3**
- **JDK 21**
- **Gradle (Kotlin DSL)**
- **PostgreSQL**
- **Apache Kafka**
- **Docker**
- **GitHub Actions**

## Структура проекта

```
src/
├── main/
│   ├── kotlin/
│   │   └── com/naidizakupku/telegram/
│   │       ├── config/          # Конфигурации
│   │       ├── controller/      # REST контроллеры
│   │       ├── domain/          # Доменные модели
│   │       ├── repository/      # Репозитории
│   │       ├── service/         # Бизнес-логика
│   │       └── TelegramApplication.kt
│   └── resources/
│       ├── application.yml      # Основная конфигурация
│       └── db/
│           └── changelog/       # Миграции Liquibase
└── test/
    └── kotlin/                  # Тесты
```

## Локальная разработка

### Требования

- JDK 21
- Docker
- Docker Compose

### Запуск

1. Клонируй репозиторий:
```bash
git clone <repository-url>
cd naidizakupku_telegram
```

2. Запусти зависимости через Docker Compose:
```bash
docker-compose up -d
```

3. Запусти приложение:
```bash
./gradlew bootRun
```

### Тестирование

```bash
./gradlew test
```

## CI/CD Pipeline

Проект использует GitHub Actions для автоматизации CI/CD.

### Workflow

1. **Test** - Запуск тестов
2. **Build** - Сборка JAR файла
3. **Docker Build** - Сборка Docker образа (только для main)
4. **Deploy** - Деплой на сервер (только для main)

### Настройка Secrets

Добавь следующие secrets в GitHub репозитории (Settings → Secrets and variables → Actions):

#### Обязательные:
- `SERVER_IP` - IP адрес сервера
- `SSH_PORT` - SSH порт (обычно 22)
- `SERVER_USER` - пользователь сервера
- `SSH_KEY` - приватный SSH ключ
- `GHCR_TOKEN` - Personal Access Token с правами `write:packages`

#### База данных:
- `POSTGRES_URL` - URL PostgreSQL
- `POSTGRES_USER` - пользователь PostgreSQL
- `POSTGRES_PASSWORD` - пароль PostgreSQL

#### Kafka:
- `KAFKA_BOOTSTRAP_SERVERS` - адреса Kafka серверов

### Создание Personal Access Token

1. Перейди в GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Создай новый токен с правами:
   - `repo` (полный доступ к репозиторию)
   - `write:packages` (запись в GitHub Container Registry)
3. Сохрани токен как `GHCR_TOKEN` в secrets

### Environment

Создай environment `production` в GitHub:
1. Settings → Environments → New environment
2. Название: `production`
3. Добавь все secrets выше

## Docker

### Сборка образа

```bash
docker build -t naidizakupku-telegram .
```

### Запуск контейнера

```bash
docker run -d \
  --name telegram-app \
  -p 8080:8080 \
  -e POSTGRES_URL=jdbc:postgresql://localhost:5432/telegram \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  -e KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
  naidizakupku-telegram
```

## Миграции базы данных

Миграции управляются через Liquibase и находятся в `src/main/resources/db/changelog/`.

### Создание новой миграции

1. Создай новый XML файл в `src/main/resources/db/changelog/changes/`
2. Добавь ссылку в `db.changelog-master.xml`
3. При запуске приложения миграции применятся автоматически

## Логирование

Логи сохраняются в `/opt/telegram-app/logs/` при запуске в Docker.

## Мониторинг

Приложение предоставляет health check endpoint:
```
GET /actuator/health
```

## Лицензия

MIT License
