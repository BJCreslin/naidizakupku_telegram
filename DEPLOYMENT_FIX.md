# Исправление проблемы с деплоем

## Проблема
При деплое приложения возникала ошибка 503 при проверке health endpoint. Основные причины:

1. **Проблема с переменными окружения** - `$POSTGRES_URL` не подставлялся в конфигурацию
2. **Проблема с Kafka** - приложение пыталось подключиться к недоступному Kafka серверу
3. **Недостаточное время ожидания** - приложение не успевало запуститься

## Решения

### 1. Исправление переменных окружения

**Проблема**: Переменная `$POSTGRES_URL` не была установлена в окружении.

**Решение**: Установить переменные окружения в PowerShell:
```powershell
$env:POSTGRES_URL="jdbc:postgresql://localhost:5432/telegram_db"
$env:POSTGRES_USER="postgres"
$env:POSTGRES_PASSWORD="password"
```

### 2. Отключение Kafka в продакшене

**Проблема**: Приложение пыталось подключиться к Kafka, который недоступен в продакшене.

**Решение**: Создан профиль `prod` в `application-prod.yml`:
```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
  kafka:
    bootstrap-servers: ""
    producer:
      enabled: false
    admin:
      auto-create: false

logging:
  level:
    org.springframework.kafka: OFF
    org.apache.kafka: OFF
```

### 3. Обновление скрипта деплоя

**Изменения в `jenkins/deploy.sh`**:

1. Добавлен профиль `prod`:
```bash
environment:
  - SPRING_PROFILES_ACTIVE=prod
```

2. Увеличено время ожидания:
```bash
sleep 60  # вместо 30
```

3. Улучшен healthcheck:
```bash
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 5  # вместо 3
  start_period: 60s  # вместо 40s
```

### 4. Исправление репозитория

**Проблема**: В `UserRepository` был метод `findByUsername`, но поле `username` было удалено из сущности.

**Решение**: Удален метод `findByUsername` из репозитория.

## Тестирование

### Локальное тестирование
```bash
# Установить переменные окружения
$env:POSTGRES_URL="jdbc:postgresql://localhost:5432/telegram_db"
$env:POSTGRES_USER="postgres"
$env:POSTGRES_PASSWORD="password"

# Запустить с профилем prod
$env:SPRING_PROFILES_ACTIVE="prod"; ./gradlew bootRun
```

### Тестирование Docker образа
```bash
# Создать образ
docker build -t ghcr.io/bjcreslin/naidizakupku-telegram:latest .

# Запустить контейнер
docker run -d --name telegram-app-test \
  -p 8081:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e POSTGRES_URL=jdbc:postgresql://host.docker.internal:5432/telegram_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  ghcr.io/bjcreslin/naidizakupku-telegram:latest

# Проверить health endpoint
curl http://localhost:8081/actuator/health
```

## Результат

После внесения исправлений:
- ✅ Приложение успешно запускается с профилем `prod`
- ✅ Health endpoint возвращает статус 200
- ✅ Kafka отключен в продакшене
- ✅ Переменные окружения корректно подставляются
- ✅ Увеличено время ожидания для стабильного деплоя

## Следующие шаги

1. Запустить новый деплой с исправленным кодом
2. Мониторить логи приложения в продакшене
3. При необходимости добавить Kafka в продакшен инфраструктуру
