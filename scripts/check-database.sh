#!/bin/bash

echo "🔍 Проверка подключения к базе данных..."

# Проверяем переменные окружения
if [ -z "$POSTGRES_URL" ]; then
    echo "❌ POSTGRES_URL не установлен"
    exit 1
fi

if [ -z "$POSTGRES_USER" ]; then
    echo "❌ POSTGRES_USER не установлен"
    exit 1
fi

if [ -z "$POSTGRES_PASSWORD" ]; then
    echo "❌ POSTGRES_PASSWORD не установлен"
    exit 1
fi

echo "✅ Переменные окружения установлены"

# Извлекаем хост и порт из URL
HOST_PORT=$(echo $POSTGRES_URL | sed 's/jdbc:postgresql:\/\///' | sed 's/\/.*//')
HOST=$(echo $HOST_PORT | cut -d: -f1)
PORT=$(echo $HOST_PORT | cut -d: -f2)

echo "🌐 Хост: $HOST"
echo "🔌 Порт: $PORT"

# Проверяем доступность порта
if nc -z $HOST $PORT 2>/dev/null; then
    echo "✅ Порт $PORT доступен на $HOST"
else
    echo "❌ Порт $PORT недоступен на $HOST"
    exit 1
fi

# Пробуем подключиться к БД
echo "🔐 Подключение к базе данных..."
PGPASSWORD=$POSTGRES_PASSWORD psql -h $HOST -p $PORT -U $POSTGRES_USER -d telegram_db -c "SELECT version();" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ Подключение к БД успешно"
    
    # Проверяем таблицы
    echo "📋 Проверка таблиц..."
    PGPASSWORD=$POSTGRES_PASSWORD psql -h $HOST -p $PORT -U $POSTGRES_USER -d telegram_db -c "\dt" 2>/dev/null
    
    # Проверяем таблицу Liquibase
    echo "🔄 Проверка таблицы Liquibase..."
    PGPASSWORD=$POSTGRES_PASSWORD psql -h $HOST -p $PORT -U $POSTGRES_USER -d telegram_db -c "SELECT COUNT(*) FROM databasechangelog;" 2>/dev/null
    
else
    echo "❌ Не удалось подключиться к БД"
    exit 1
fi
