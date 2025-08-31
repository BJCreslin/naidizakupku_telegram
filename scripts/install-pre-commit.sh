#!/bin/bash

# Скрипт для установки pre-commit hook

echo "🔧 Установка pre-commit hook для проверки архитектурной документации..."

# Проверяем, что мы в git репозитории
if [ ! -d ".git" ]; then
    echo "❌ ОШИБКА: Не найден .git каталог. Запустите скрипт из корня git репозитория."
    exit 1
fi

# Создаем .git/hooks каталог если его нет
mkdir -p .git/hooks

# Копируем pre-commit hook
cp scripts/pre-commit-hook.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit

echo "✅ Pre-commit hook установлен успешно!"
echo ""
echo "📋 Что делает этот hook:"
echo "   - Проверяет наличие файлов ARCHITECTURE.md и ARCHITECTURE_RULES.md"
echo "   - Предупреждает, если код изменен, но документация не обновлена"
echo "   - Проверяет синтаксис Markdown (если установлен markdownlint)"
echo "   - Проверяет наличие обязательных разделов в документации"
echo "   - Предупреждает о классах, не описанных в документации"
echo ""
echo "🔧 Для установки markdownlint (опционально):"
echo "   npm install -g markdownlint-cli"
echo ""
echo "📚 Документация по правилам: ARCHITECTURE_RULES.md"
