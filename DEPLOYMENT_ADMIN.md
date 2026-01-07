# Деплой и настройка админки

## Обзор

Админка интегрирована в Spring Boot приложение и раздается как статические файлы через тот же сервер.

## Структура деплоя

1. **Frontend сборка**: Админка собирается через Vite в production режиме
2. **Интеграция**: Собранные файлы копируются в `src/main/resources/static/admin/`
3. **Раздача**: Spring Boot раздает статические файлы по пути `/admin/**`

## Локальная сборка

### Сборка админки отдельно

```bash
cd admin-panel
npm install
npm run build
```

### Сборка всего приложения (включая админку)

```bash
# Сначала соберите админку
cd admin-panel
npm install
npm run build
cd ..

# Затем соберите backend (админка будет автоматически скопирована)
./gradlew clean build
```

Или используйте Gradle задачу:

```bash
./gradlew buildAdminPanel copyAdminPanel build
```

## Docker сборка

Dockerfile использует multi-stage build:

1. **Stage 1 (frontend-build)**: Собирает админку через Node.js
2. **Stage 2 (build)**: Копирует собранную админку и собирает Spring Boot приложение
3. **Stage 3 (runtime)**: Создает runtime образ с JAR файлом

```bash
docker build -t naidizakupku-telegram .
```

## CI/CD

GitHub Actions автоматически:

1. Собирает админку в отдельном job (`build-frontend`)
2. Копирует собранные файлы в `src/main/resources/static/admin/`
3. Собирает Spring Boot приложение с включенной админкой
4. Создает Docker образ

## Создание первого администратора

### Через API

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

### Через скрипт

**Linux/Mac:**
```bash
./scripts/create-first-admin.sh admin securepassword123 admin@example.com
```

**Windows:**
```cmd
scripts\create-first-admin.bat admin securepassword123 admin@example.com
```

### Важно

- Endpoint `/api/admin/auth/register-first-admin` доступен только если в системе нет администраторов
- После создания первого администратора этот endpoint становится недоступным
- Используйте сильные пароли в production

## Доступ к админке

После деплоя админка доступна по адресу:

- **Локально**: `http://localhost:8080/admin`
- **Production**: `https://your-domain.com/admin`

## Environment Variables

### Frontend (Vite)

- `VITE_API_URL` - URL API (опционально, по умолчанию используется относительный путь)

В production админка использует относительный путь к API (`/api`), так как раздается через тот же домен.

### Backend

- `ADMIN_JWT_SECRET` - Секретный ключ для JWT токенов (обязательно в production!)
- `ADMIN_JWT_ACCESS_EXPIRATION_MINUTES` - Время жизни access token (по умолчанию 60 минут)
- `ADMIN_JWT_REFRESH_EXPIRATION_DAYS` - Время жизни refresh token (по умолчанию 7 дней)

## CORS настройки

CORS настроен для работы с админкой:

- Разрешены все origins (можно ограничить в production)
- Разрешены методы: GET, POST, PUT, DELETE, OPTIONS, PATCH
- Разрешены credentials
- Max age для preflight: 1 час

В production рекомендуется ограничить `allowedOriginPatterns` конкретными доменами.

## Troubleshooting

### Админка не загружается

1. Проверьте, что админка собрана: `ls src/main/resources/static/admin/`
2. Проверьте логи Spring Boot на наличие ошибок
3. Убедитесь, что путь `/admin/**` разрешен в SecurityConfig

### API запросы не работают

1. Проверьте CORS настройки
2. Убедитесь, что `VITE_API_URL` не установлен (для production)
3. Проверьте, что API доступен по пути `/api/admin/**`

### SPA routing не работает

WebMvcConfig настроен для поддержки SPA routing - все запросы к `/admin/**` без расширения файла возвращают `index.html`.

## Production рекомендации

1. **Безопасность**:
   - Измените `ADMIN_JWT_SECRET` на сильный случайный ключ
   - Ограничьте CORS origins конкретными доменами
   - Используйте HTTPS

2. **Производительность**:
   - Статические файлы кешируются на 1 час (HTML) и 24 часа (assets)
   - Используйте CDN для статических файлов (опционально)

3. **Мониторинг**:
   - Настройте логирование доступа к админке
   - Мониторьте неудачные попытки входа

