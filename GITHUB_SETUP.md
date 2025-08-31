# Настройка GitHub Actions CI/CD

## Настройка Secrets

Для работы GitHub Actions pipeline необходимо настроить следующие secrets в настройках репозитория:

### Перейти в Settings → Secrets and variables → Actions

Добавьте следующие Repository secrets:

### Подключение к VPS
- **REMOTE_HOST**: `5.44.40.79` - IP адрес VPS сервера
- **REMOTE_USER**: `root` - пользователь для SSH подключения
- **SSH_PRIVATE_KEY**: Приватный SSH ключ для подключения к VPS

### GitFlic Registry
- **GITFLIC_USER**: Ваш логин в GitFlic
- **GITFLIC_PASS**: Ваш пароль или токен для GitFlic registry

### База данных
- **POSTGRES_PASSWORD**: Пароль от PostgreSQL базы данных

## Настройка SSH ключа

1. **Создайте SSH ключ** (если его нет):
```bash
ssh-keygen -t rsa -b 4096 -C "github-actions"
```

2. **Скопируйте публичный ключ на VPS**:
```bash
ssh-copy-id root@5.44.40.79
```

3. **Добавьте приватный ключ в GitHub Secrets**:
   - Скопируйте содержимое файла `~/.ssh/id_rsa`
   - Добавьте его в secret `SSH_PRIVATE_KEY`

## Workflow Pipeline

### Этапы CI/CD:

1. **Test** - запуск тестов на каждом pull request
2. **Build** - сборка Docker образа при мерже в main
3. **Deploy** - автоматический деплой на VPS

### Триггеры:

- **Push в main** - полный pipeline (test → build → deploy)
- **Pull Request** - только тесты

## Мониторинг деплоя

### Проверка статуса:
- GitHub Actions tab в репозитории
- Логи деплоя в Actions

### Проверка приложения на VPS:
```bash
# Статус контейнера
ssh root@5.44.40.79 "docker ps | grep telegram-app"

# Логи приложения
ssh root@5.44.40.79 "docker logs telegram-app -f"

# Health check
curl http://5.44.40.79:8080/actuator/health
```

## Быстрое исправление

Если что-то пошло не так, используйте скрипт быстрого исправления:

```bash
# Замените your_password на реальный пароль в скрипте
./scripts/fix-app.sh
```

## Отладка проблем

### Проблемы с SSH:
```bash
# Тест SSH подключения
ssh -o ConnectTimeout=10 root@5.44.40.79 "echo 'Connection successful'"
```

### Проблемы с Docker registry:
```bash
# Тест авторизации в GitFlic
echo $GITFLIC_PASS | docker login -u $GITFLIC_USER --password-stdin registry.gitflic.ru
```

### Проблемы с базой данных:
```bash
# Проверка подключения к PostgreSQL
docker run --rm postgres:15 psql -h 5.44.40.79 -U postgres -d telegram_db -c "SELECT version();"
```
