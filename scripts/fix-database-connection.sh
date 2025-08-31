#!/bin/bash

# Скрипт для исправления проблемы с подключением к базе данных
echo "🔧 Исправление проблемы с подключением к базе данных..."

# Проверяем, что мы в корневой директории проекта
if [ ! -f "build.gradle.kts" ]; then
    echo "❌ Ошибка: Запустите скрипт из корневой директории проекта"
    exit 1
fi

echo "📋 Проверка текущих настроек..."

# Проверяем, запущен ли PostgreSQL
if docker ps | grep -q postgres; then
    echo "✅ PostgreSQL контейнер запущен"
else
    echo "⚠️  PostgreSQL контейнер не найден"
    echo "🚀 Запускаем PostgreSQL..."
    docker-compose up -d postgres
fi

# Проверяем переменные окружения
if [ -f ".env" ]; then
    echo "✅ Файл .env найден"
    
    # Проверяем POSTGRES_URL
    if grep -q "POSTGRES_URL" .env; then
        echo "✅ POSTGRES_URL настроен"
        grep "POSTGRES_URL" .env
    else
        echo "❌ POSTGRES_URL не найден в .env"
        echo "📝 Добавьте в .env:"
        echo "   POSTGRES_URL=jdbc:postgresql://postgres:5432/telegram_db"
    fi
else
    echo "⚠️  Файл .env не найден"
    echo "📝 Создайте .env файл из env.example:"
    echo "   cp env.example .env"
fi

echo ""
echo "🔍 Возможные решения:"
echo ""
echo "1. Для локальной разработки с Docker Compose:"
echo "   POSTGRES_URL=jdbc:postgresql://postgres:5432/telegram_db"
echo ""
echo "2. Для продакшена (PostgreSQL на VPS):"
echo "   POSTGRES_URL=jdbc:postgresql://5.44.40.79:5432/telegram_db"
echo ""
echo "3. Для локальной разработки без Docker:"
echo "   POSTGRES_URL=jdbc:postgresql://localhost:5432/telegram_db"
echo ""
echo "🚀 Запуск приложения:"
echo "   # С профилем dev (Docker Compose)"
echo "   ./gradlew bootRun --args='--spring.profiles.active=dev'"
echo ""
echo "   # С профилем prod (VPS)"
echo "   ./gradlew bootRun --args='--spring.profiles.active=prod'"
