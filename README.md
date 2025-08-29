# Naidizakupku Telegram Application

Spring Boot приложение на Kotlin для работы с Telegram API, включающее CI/CD pipeline с Jenkins и Docker деплоем.

## Технологии

- **Kotlin 2.0** - основной язык разработки
- **Spring Boot 3.3** - фреймворк
- **JDK 21** - Java runtime
- **Gradle Kotlin DSL** - система сборки
- **PostgreSQL** - база данных
- **Apache Kafka** - обмен сообщениями
- **Liquibase** - миграции БД
- **Docker** - контейнеризация
- **Jenkins** - CI/CD
- **Kotest + Mockk** - тестирование

## Архитектура

```
src/main/kotlin/com/naidizakupku/telegram/
├── TelegramApplication.kt          # Главный класс приложения
├── controller/                     # REST контроллеры
│   └── UserController.kt
├── service/                        # Бизнес-логика
│   └── UserService.kt
├── repository/                     # Доступ к данным
│   └── UserRepository.kt
├── domain/                         # Доменные модели
│   └── User.kt
└── config/                         # Конфигурации
    ├── DatabaseConfig.kt
    └── KafkaConfig.kt
```

## Быстрый старт

### Локальная разработка

1. **Клонируйте репозиторий:**
```bash
git clone <repository-url>
cd naidizakupku_telegram
```

2. **Запустите зависимости через Docker Compose:**
```bash
docker-compose up -d postgres kafka zookeeper
```

3. **Соберите и запустите приложение:**
```bash
./gradlew bootRun
```

### Сборка и тестирование

```bash
# Сборка
./gradlew build

# Тесты
./gradlew test

# Запуск с профилем
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Docker

```bash
# Сборка образа
docker build -t naidizakupku/telegram-app .

# Запуск с docker-compose
docker-compose up -d

# Проверка логов
docker-compose logs -f app
```

## API Endpoints

### Пользователи

- `POST /api/users` - Создать пользователя
- `GET /api/users/{telegramId}` - Получить пользователя
- `GET /api/users` - Получить всех активных пользователей
- `PUT /api/users/{telegramId}` - Обновить пользователя
- `DELETE /api/users/{telegramId}` - Деактивировать пользователя

### Health Check

- `GET /actuator/health` - Проверка здоровья приложения
- `GET /actuator/info` - Информация о приложении
- `GET /actuator/metrics` - Метрики

## Конфигурация

### Переменные окружения

```bash
# База данных
POSTGRES_URL=jdbc:postgresql://localhost:5432/telegram_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_USER=your_kafka_user
KAFKA_PASSWORD=your_kafka_password

# Приложение
SERVER_PORT=8080
```

### Профили

- `dev` - разработка (H2, локальный Kafka)
- `prod` - продакшн (PostgreSQL, внешний Kafka)

## CI/CD Pipeline

### Jenkins Pipeline Stages

1. **Checkout** - Клонирование кода
2. **Build** - Сборка с Gradle
3. **Test** - Запуск тестов
4. **SonarQube Analysis** - Анализ кода
5. **Build Docker Image** - Сборка Docker образа
6. **Push Docker Image** - Пуш в registry
7. **Deploy to Production** - Деплой на Ubuntu сервер

### Настройка Jenkins

1. **Credentials:**
   - `docker-registry` - логин/пароль для Docker registry
   - `ssh-key` - SSH ключ для доступа к серверу

2. **Переменные окружения:**
   - `REMOTE_HOST` - IP адрес Ubuntu сервера
   - `REMOTE_USER` - пользователь для деплоя
   - `REMOTE_PATH` - путь на сервере

### Деплой на Ubuntu 20.04

1. **Установите Docker:**
```bash
sudo apt update
sudo apt install docker.io docker-compose
sudo usermod -aG docker $USER
```

2. **Создайте пользователя deploy:**
```bash
sudo useradd -m -s /bin/bash deploy
sudo usermod -aG docker deploy
```

3. **Настройте SSH доступ:**
```bash
sudo mkdir -p /home/deploy/.ssh
sudo cp ~/.ssh/authorized_keys /home/deploy/.ssh/
sudo chown -R deploy:deploy /home/deploy/.ssh
```

## Мониторинг

### Логи

- Файловые логи: `logs/application.log`
- Docker логи: `docker-compose logs -f app`

### Метрики

- Prometheus: `http://localhost:8080/actuator/prometheus`
- Health check: `http://localhost:8080/actuator/health`

## Разработка

### Добавление новой функциональности

1. Создайте доменную модель в `domain/`
2. Добавьте репозиторий в `repository/`
3. Реализуйте сервис в `service/`
4. Создайте контроллер в `controller/`
5. Напишите тесты в `src/test/kotlin/`
6. Добавьте миграцию в `src/main/resources/db/changelog/`

### Стиль кода

- Следуйте официальному гайдлайну JetBrains для Kotlin
- Используйте KDoc для публичных методов
- Пишите тесты для всех публичных методов
- Используйте корутины для асинхронных операций

## Лицензия

MIT License
