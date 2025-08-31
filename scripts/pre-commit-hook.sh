#!/bin/bash

# Pre-commit hook для проверки архитектурной документации

echo "🔍 Проверка архитектурной документации..."

# Проверяем наличие файла архитектуры
if [ ! -f "ARCHITECTURE.md" ]; then
    echo "❌ ОШИБКА: Файл ARCHITECTURE.md отсутствует!"
    echo "   Создайте файл с описанием архитектуры приложения"
    exit 1
fi

# Проверяем наличие файла правил
if [ ! -f "ARCHITECTURE_RULES.md" ]; then
    echo "❌ ОШИБКА: Файл ARCHITECTURE_RULES.md отсутствует!"
    echo "   Создайте файл с правилами поддержания документации"
    exit 1
fi

# Проверяем изменения в коде
CHANGED_FILES=$(git diff --cached --name-only --diff-filter=ACM | grep -E '\.(kt|java|xml|yml|yaml)$')

if [ -n "$CHANGED_FILES" ]; then
    echo "📝 Обнаружены изменения в коде:"
    echo "$CHANGED_FILES"
    
    # Проверяем, есть ли изменения в ARCHITECTURE.md
    if ! git diff --cached --name-only | grep -q "ARCHITECTURE.md"; then
        echo "⚠️  ПРЕДУПРЕЖДЕНИЕ: Изменения в коде обнаружены, но ARCHITECTURE.md не обновлен"
        echo "   Убедитесь, что архитектурная документация актуальна"
        echo "   См. ARCHITECTURE_RULES.md для правил обновления"
        
        # Спрашиваем пользователя, хочет ли он продолжить
        read -p "Продолжить коммит? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo "❌ Коммит отменен"
            exit 1
        fi
    else
        echo "✅ ARCHITECTURE.md обновлен"
    fi
fi

# Проверяем синтаксис Markdown (если доступен markdownlint)
if command -v markdownlint >/dev/null 2>&1; then
    echo "🔍 Проверка синтаксиса Markdown..."
    if ! markdownlint ARCHITECTURE.md ARCHITECTURE_RULES.md; then
        echo "❌ ОШИБКА: Проблемы с синтаксисом Markdown"
        exit 1
    fi
    echo "✅ Синтаксис Markdown корректен"
else
    echo "⚠️  markdownlint не установлен, пропускаем проверку синтаксиса"
fi

# Проверяем наличие обязательных разделов в ARCHITECTURE.md
echo "🔍 Проверка структуры документации..."

REQUIRED_SECTIONS=(
    "## Технологический стек"
    "## Архитектурные слои"
    "## Поток данных"
    "## Структура базы данных"
    "## Конфигурация"
)

for section in "${REQUIRED_SECTIONS[@]}"; do
    if ! grep -q "^$section$" ARCHITECTURE.md; then
        echo "❌ ОШИБКА: Отсутствует обязательный раздел: $section"
        exit 1
    fi
done

echo "✅ Все обязательные разделы присутствуют"

# Проверяем актуальность описания классов
echo "🔍 Проверка актуальности описания классов..."

# Получаем список Kotlin файлов в проекте
KOTLIN_FILES=$(find src/main/kotlin -name "*.kt" -type f 2>/dev/null || true)

if [ -n "$KOTLIN_FILES" ]; then
    for file in $KOTLIN_FILES; do
        # Извлекаем имя класса из файла
        CLASS_NAME=$(basename "$file" .kt)
        
        # Проверяем, упоминается ли класс в документации
        if ! grep -q "$CLASS_NAME" ARCHITECTURE.md; then
            echo "⚠️  ПРЕДУПРЕЖДЕНИЕ: Класс $CLASS_NAME не описан в ARCHITECTURE.md"
        fi
    done
fi

echo "✅ Проверка архитектурной документации завершена"
echo "📚 Не забудьте обновить документацию при изменении архитектуры!"
