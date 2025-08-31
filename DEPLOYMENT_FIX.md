# Исправление проблемы с подключением к базе данных

## Проблема
Приложение в контейнере не может подключиться к PostgreSQL, запущенному локально на VPS.

## Причина
В `application-prod.yml` был указан IP `5.44.40.79:5432`, но контейнер не может подключиться к этому адресу.

## Решение

### 1. Исправлена конфигурация
- Изменен URL в `application-prod.yml`: `host.docker.internal:5432`
- Убран явный диалект PostgreSQL (Hibernate сам определит)
- Добавлен `extra_hosts` в docker-compose.prod.yml

### 2. Обновлен GitHub Actions CI/CD
- Добавлен `--add-host=host.docker.internal:host-gateway` для подключения к локальной БД
- Добавлены переменные окружения для Telegram бота
- Исправлен URL подключения к БД

### 3. Скрипты для исправления
- `scripts/fix-database-connection.sh` - для Linux
- `scripts/fix-database-connection.bat` - для Windows

## Инструкция по развертыванию

### На VPS с Ubuntu:

1. **Убедитесь, что PostgreSQL запущен локально:**
```bash
sudo systemctl status postgresql
sudo systemctl start postgresql  # если не запущен
```

2. **Создайте базу данных и пользователя:**
```bash
sudo -u postgres psql
CREATE DATABASE telegram_db;
CREATE USER your_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE telegram_db TO your_user;
\q
```

3. **Установите переменные окружения:**
```bash
export POSTGRES_USER=your_user
export POSTGRES_PASSWORD=your_password
export POSTGRES_DB=telegram_db
```

4. **Приложение автоматически деплоится через GitHub Actions при пуше в main**

5. **Проверьте логи:**
```bash
docker logs -f telegram-app
```

### Альтернативное решение

Если `host.docker.internal` не работает, используйте IP хоста:

1. **Найдите IP хоста:**
```bash
ip addr show | grep inet
```

2. **Измените POSTGRES_URL в GitHub Secrets:**
```bash
# В настройках репозитория GitHub -> Secrets and variables -> Actions
POSTGRES_URL=jdbc:postgresql://host.docker.internal:5432/telegram_db
```

## Проверка подключения

### Тест подключения к PostgreSQL:
```bash
# Из контейнера
docker exec -it telegram_app_prod bash
apt-get update && apt-get install -y postgresql-client
psql -h host.docker.internal -U your_user -d telegram_db
```

### Проверка логов приложения:
```bash
docker-compose -f docker-compose.prod.yml logs -f app | grep -i "database\|postgres\|hibernate"
```

## Возможные проблемы

### 1. PostgreSQL не принимает подключения
Проверьте `pg_hba.conf`:
```bash
sudo nano /etc/postgresql/*/main/pg_hba.conf
```
Добавьте строку:
```
host    all             all             172.17.0.0/16           md5
```

### 2. Firewall блокирует подключения
```bash
sudo ufw allow 5432
```

### 3. PostgreSQL не слушает внешние подключения
Проверьте `postgresql.conf`:
```bash
sudo nano /etc/postgresql/*/main/postgresql.conf
```
Убедитесь, что:
```
listen_addresses = '*'
port = 5432
```
