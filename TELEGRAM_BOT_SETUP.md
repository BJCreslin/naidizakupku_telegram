# Настройка Telegram бота

## Создание бота

1. Откройте Telegram и найдите @BotFather
2. Отправьте команду `/newbot`
3. Введите имя бота (например, "NaidiZakupku Bot")
4. Введите username бота (должен заканчиваться на 'bot', например, "naidizakupku_bot")
5. BotFather выдаст токен бота - сохраните его

## Настройка переменных окружения

Добавьте следующие переменные в файл `.env`:

```bash
# Telegram Bot Configuration
TELEGRAM_BOT_TOKEN=your_telegram_bot_token_here
TELEGRAM_BOT_NAME=your_telegram_bot_name_here
TELEGRAM_BOT_USERNAME=your_telegram_bot_username_here
```

## Запуск бота

1. Убедитесь, что PostgreSQL и Kafka запущены:
```bash
docker-compose up -d
```

2. Запустите приложение:
```bash
./gradlew bootRun
```

3. Найдите вашего бота в Telegram по username и отправьте ему сообщение

## Функционал

### Текущий функционал
- **Эхо-функция**: Бот отвечает "Эхо: [ваше сообщение]"
- **Автоматическое сохранение пользователей**: При первом сообщении пользователь сохраняется в базе данных
- **Обновление данных пользователя**: При каждом сообщении обновляется информация о пользователе

### Планируемый функционал
- Поиск товаров
- Покупка товаров
- Уведомления о скидках
- Интеграция с внешними API

## Структура данных

### Пользователи (users)
- `id` - уникальный идентификатор
- `telegram_id` - ID пользователя в Telegram
- `first_name` - имя пользователя
- `last_name` - фамилия пользователя
- `username` - username пользователя
- `created_at` - время создания записи
- `updated_at` - время последнего обновления
- `active` - статус активности

## Логирование

Логи бота записываются в файл `logs/application.log` с уровнем INFO для пакета `org.telegram`.

## Безопасность

- Токен бота хранится в переменных окружения
- Все входящие сообщения логируются
- Пользователи сохраняются в базе данных для аналитики

## Мониторинг

- Health check: `http://localhost:8080/actuator/health`
- Метрики: `http://localhost:8080/actuator/metrics`
- Prometheus: `http://localhost:8080/actuator/prometheus`
