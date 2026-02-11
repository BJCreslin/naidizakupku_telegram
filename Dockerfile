# Multi-stage build для оптимизации размера образа
FROM node:20-alpine AS frontend-build

# Собираем админку
WORKDIR /app/admin-panel
COPY admin-panel/package*.json ./
RUN npm install
COPY admin-panel ./
RUN npm run build

# Сборка backend
FROM gradle:8.8-jdk21 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы конфигурации Gradle
COPY gradle gradle
COPY gradlew build.gradle.kts gradle.properties ./

# Скачиваем зависимости
RUN gradle dependencies --no-daemon

# Копируем собранную админку из frontend-build
COPY --from=frontend-build /app/admin-panel/dist src/main/resources/static/admin

# Копируем исходный код
COPY src src

# Собираем приложение
RUN gradle build --no-daemon

# Второй этап - создание runtime образа
FROM eclipse-temurin:21-jre

# Устанавливаем рабочую директорию
WORKDIR /app

# Создаем пользователя для безопасности
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Копируем собранный JAR файл
COPY --from=build /app/build/libs/*.jar app.jar

# Создаем директорию для логов с правильными правами
RUN mkdir -p /app/logs && chown -R appuser:appuser /app/logs && chmod -R 755 /app/logs

# Переключаемся на пользователя приложения
USER appuser

# Открываем порт
EXPOSE 8080

# Устанавливаем переменные окружения
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Команда запуска приложения
ENTRYPOINT ["/usr/bin/java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]