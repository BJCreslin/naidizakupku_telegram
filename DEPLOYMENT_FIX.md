# Исправление проблем деплоя

## Проблема с подключением к базе данных

### Описание проблемы
При запуске приложения в Docker контейнере возникает ошибка:
```
Unable to determine Dialect without JDBC metadata (please set 'jakarta.persistence.jdbc.url' for common cases or 'hibernate.dialect' when a custom Dialect implementation must be provided)
```

Это происходит потому, что приложение в Docker контейнере не может подключиться к PostgreSQL по адресу `localhost`.

### Решение

#### Автоматическое исправление
Запустите один из скриптов:

**Linux/macOS:**
```bash
./scripts/fix-database-connection.sh
```

**Windows:**
```cmd
scripts\fix-database-connection.bat
```

#### Ручное исправление

1. **Для локальной разработки с Docker Compose:**
   ```bash
   POSTGRES_URL=jdbc:postgresql://postgres:5432/telegram_db
   ```

2. **Для продакшена (PostgreSQL на VPS):**
   ```bash
   POSTGRES_URL=jdbc:postgresql://5.44.40.79:5432/telegram_db
   ```

3. **Для локальной разработки без Docker:**
   ```bash
   POSTGRES_URL=jdbc:postgresql://localhost:5432/telegram_db
   ```

4. **Запуск с правильным профилем:**
   ```bash
   # С профилем dev (Docker Compose)
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   
   # С профилем prod (VPS)
   ./gradlew bootRun --args='--spring.profiles.active=prod'
   ```

### Проверка исправления

После исправления приложение должно запускаться без ошибок подключения к БД.

## Проблема с JAXB в Java 21

### Описание проблемы
При запуске приложения в Java 21 возникает ошибка:
```
java.lang.NoClassDefFoundError: javax/xml/bind/annotation/XmlElement
```

Это происходит потому, что модуль JAXB был удален из JDK начиная с Java 9.

### Решение

#### Автоматическое исправление
Запустите один из скриптов:

**Linux/macOS:**
```bash
./scripts/fix-jaxb-issue.sh
```

**Windows:**
```cmd
scripts\fix-jaxb-issue.bat
```

#### Ручное исправление

1. **Добавьте зависимости в `build.gradle.kts`:**
```kotlin
// JAXB API для Java 21
implementation("javax.xml.bind:jaxb-api:2.3.1")
implementation("org.glassfish.jaxb:jaxb-runtime:4.0.4")
```

2. **Удалите явное указание диалекта PostgreSQL из `application.yml`:**
```yaml
jpa:
  properties:
    hibernate:
      # Удалите строку: dialect: org.hibernate.dialect.PostgreSQLDialect
      format_sql: true
```

3. **Пересоберите проект:**
```bash
./gradlew clean build
```

### Проверка исправления

После исправления приложение должно запускаться без ошибок:

```bash
./gradlew bootRun
```

Логи должны показывать успешный запуск:
```
2025-08-31 11:13:07 [main] INFO  c.n.telegram.TelegramApplicationKt - Starting TelegramApplicationKt
...
2025-08-31 11:13:15 [main] INFO  c.n.telegram.TelegramApplicationKt - Started TelegramApplicationKt
```

## Другие проблемы

### Проблема с подключением к базе данных
Если возникает ошибка подключения к PostgreSQL:

1. **Проверьте переменные окружения:**
```bash
POSTGRES_URL=jdbc:postgresql://localhost:5432/telegram_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password
```

2. **Убедитесь, что PostgreSQL запущен:**
```bash
docker-compose up -d postgres
```

### Проблема с Kafka
Если Kafka не запускается:

1. **Проверьте конфигурацию:**
```bash
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

2. **Перезапустите Kafka:**
```bash
docker-compose restart kafka
```

## Полезные команды

### Проверка статуса сервисов
```bash
# Статус Docker контейнеров
docker-compose ps

# Логи приложения
docker-compose logs -f app

# Логи PostgreSQL
docker-compose logs -f postgres

# Логи Kafka
docker-compose logs -f kafka
```

### Очистка и пересборка
```bash
# Очистка проекта
./gradlew clean

# Пересборка
./gradlew build

# Пересборка Docker образа
docker-compose build --no-cache
```

### Проверка здоровья приложения
```bash
# Health check
curl http://localhost:8080/actuator/health

# Метрики
curl http://localhost:8080/actuator/metrics
```
