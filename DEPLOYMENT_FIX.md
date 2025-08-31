# Исправление проблем деплоя

## Проблемы, которые были исправлены

### 1. Проблема с переменными окружения PostgreSQL

**Проблема:** В логах видно, что переменные окружения не подставляются в контейнер:
```
Driver org.postgresql.Driver claims to not accept jdbcUrl, $POSTGRES_URL
```

**Причина:** В GitHub Actions workflow использовались одинарные кавычки вместо двойных:
```bash
# Неправильно
-e POSTGRES_URL='$POSTGRES_URL'

# Правильно  
-e POSTGRES_URL="$POSTGRES_URL"
```

**Исправление:** Обновлен файл `.github/workflows/ci-cd.yml`

### 2. Проблема с конфигурацией Kafka

**Проблема:** Конфликт между auto commit и MANUAL_IMMEDIATE ack mode:
```
Consumer cannot be configured for auto commit for ackMode MANUAL_IMMEDIATE
```

**Причина:** В конфигурации было включено `enable-auto-commit: true` при использовании `ackMode: MANUAL_IMMEDIATE`

**Исправление:**
- Отключен auto commit в `application-prod.yml`
- Отключен auto commit в `KafkaConfig.kt`
- Добавлена условная загрузка Kafka компонентов

### 3. Проблема с недоступностью Kafka

**Проблема:** Приложение падает если Kafka недоступен

**Исправление:**
- Добавлена условная загрузка Kafka компонентов через `@ConditionalOnProperty`
- В продакшене Kafka отключается если `KAFKA_BOOTSTRAP_SERVERS` не указан

## Как применить исправления

### Автоматически через GitHub Actions

1. Закоммитьте изменения:
```bash
git add .
git commit -m "Fix deployment issues: PostgreSQL env vars and Kafka config"
git push origin main
```

2. GitHub Actions автоматически запустит новый деплой с исправлениями

### Вручную на сервере

1. Запустите скрипт исправления:
```bash
# Установите переменные окружения
export GHCR_TOKEN="your_ghcr_token"
export POSTGRES_URL="your_postgres_url"
export POSTGRES_USER="your_postgres_user"
export POSTGRES_PASSWORD="your_postgres_password"

# Запустите скрипт
./scripts/fix-deployment.sh
```

## Проверка исправлений

### 1. Проверка переменных окружения

```bash
# Проверьте логи контейнера
docker logs telegram-app | grep "POSTGRES_URL"

# Должно показать реальный URL, а не $POSTGRES_URL
```

### 2. Проверка Kafka

```bash
# Проверьте что приложение запустилось без ошибок Kafka
docker logs telegram-app | grep -i kafka

# Если Kafka отключен, не должно быть ошибок подключения
```

### 3. Проверка здоровья приложения

```bash
# Проверьте health endpoint
curl http://localhost:8080/actuator/health

# Должен вернуть {"status":"UP"}
```

## Дополнительные улучшения

### 1. Добавлена условная загрузка Kafka

Теперь приложение может работать без Kafka:
- Если `KAFKA_BOOTSTRAP_SERVERS` не указан, Kafka компоненты не загружаются
- Приложение запускается только с базовой функциональностью

### 2. Улучшен скрипт деплоя

- Добавлены проверки переменных окружения
- Улучшена диагностика проблем
- Добавлены эмодзи для лучшей читаемости логов

### 3. Обновлена документация

- Создан файл `DEPLOYMENT_FIX.md` с описанием проблем и решений
- Добавлен скрипт `fix-deployment.sh` для быстрого исправления

## Мониторинг

После исправления следите за логами:

```bash
# Мониторинг логов в реальном времени
docker logs -f telegram-app

# Проверка статуса контейнера
docker ps | grep telegram-app

# Проверка использования ресурсов
docker stats telegram-app
```
