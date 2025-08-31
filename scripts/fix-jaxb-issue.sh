#!/bin/bash

# Скрипт для исправления проблемы с JAXB в Java 21
echo "🔧 Исправление проблемы с JAXB в Java 21..."

# Проверяем, что мы в корневой директории проекта
if [ ! -f "build.gradle.kts" ]; then
    echo "❌ Ошибка: Запустите скрипт из корневой директории проекта"
    exit 1
fi

# Очищаем и пересобираем проект
echo "📦 Очистка и пересборка проекта..."
./gradlew clean build

if [ $? -eq 0 ]; then
    echo "✅ Проект успешно собран!"
    echo ""
    echo "🔍 Проверьте, что в build.gradle.kts добавлены зависимости:"
    echo "   implementation(\"javax.xml.bind:jaxb-api:2.3.1\")"
    echo "   implementation(\"org.glassfish.jaxb:jaxb-runtime:4.0.4\")"
    echo ""
    echo "🚀 Теперь можно запускать приложение:"
    echo "   ./gradlew bootRun"
else
    echo "❌ Ошибка при сборке проекта"
    exit 1
fi
