# Исправление проблемы с Liquibase

## Проблема
Liquibase не создает таблицы при запуске приложения.

## Причины и решения

### 1. Отсутствует зависимость Liquibase
**Проблема**: В `build.gradle.kts` не была добавлена зависимость `org.liquibase:liquibase-core`

**Решение**: ✅ Исправлено - добавлена зависимость в build.gradle.kts

### 2. Проверка конфигурации

#### application.yml
```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    enabled: true
```

#### Структура файлов
```
src/main/resources/
├── db/
│   ├── changelog/
│   │   ├── db.changelog-master.xml
│   │   └── changes/
│   │       └── 001-create-users-table.xml
```

### 3. Проверка подключения к БД

#### Запуск скрипта проверки
```bash
# Windows
scripts\check-database.bat

# Linux/WSL
scripts/check-database.sh
```

#### Проверка переменных окружения
```bash
echo $POSTGRES_URL
echo $POSTGRES_USER
echo $POSTGRES_PASSWORD
```

### 4. Ручное создание таблиц (если Liquibase не работает)

#### Подключение к PostgreSQL
```bash
psql -h localhost -p 5432 -U postgres -d telegram_db
```

#### Создание таблицы users
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    telegram_id BIGINT UNIQUE NOT NULL,
    username VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);

-- Создание индексов
CREATE INDEX idx_users_telegram_id ON users(telegram_id);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_active ON users(active);
```

### 5. Проверка логов

#### Включение debug логирования
```yaml
logging:
  level:
    liquibase: DEBUG
    org.springframework.jdbc: DEBUG
```

#### Поиск ошибок в логах
```bash
grep -i "liquibase\|database\|table" logs/application.log
```

### 6. Тестирование

#### Запуск приложения
```bash
./gradlew bootRun
```

#### Проверка в StartupInfoService
При запуске приложения автоматически выводится:
- Статус подключения к БД
- Список найденных таблиц
- Количество примененных изменений Liquibase

### 7. Возможные проблемы

#### Проблема с правами доступа
```sql
-- Проверка прав пользователя
SELECT grantee, privilege_type, table_name 
FROM information_schema.role_table_grants 
WHERE grantee = 'postgres';
```

#### Проблема с кодировкой
```sql
-- Проверка кодировки БД
SHOW server_encoding;
SHOW client_encoding;
```

### 8. Альтернативные решения

#### Использование Hibernate DDL
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # Только для разработки!
```

#### Ручные миграции
```bash
# Создание SQL скрипта
pg_dump -h localhost -U postgres -d telegram_db --schema-only > schema.sql

# Применение изменений
psql -h localhost -U postgres -d telegram_db < schema.sql
```

## Контакты
При возникновении проблем проверьте:
1. Логи приложения
2. Подключение к БД
3. Права доступа пользователя
4. Конфигурацию Liquibase
