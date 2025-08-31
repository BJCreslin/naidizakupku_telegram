@echo off
echo 🔧 Установка pre-commit hook для Windows...

REM Проверяем, что мы в git репозитории
if not exist ".git" (
    echo ❌ ОШИБКА: Не найден .git каталог. Запустите скрипт из корня git репозитория.
    exit /b 1
)

REM Создаем .git/hooks каталог если его нет
if not exist ".git\hooks" mkdir .git\hooks

REM Копируем pre-commit hook
copy "scripts\pre-commit-hook.sh" ".git\hooks\pre-commit" >nul

echo ✅ Pre-commit hook установлен успешно!
echo.
echo 📋 Что делает этот hook:
echo    - Проверяет наличие файлов ARCHITECTURE.md и ARCHITECTURE_RULES.md
echo    - Предупреждает, если код изменен, но документация не обновлена
echo    - Проверяет синтаксис Markdown (если установлен markdownlint)
echo    - Проверяет наличие обязательных разделов в документации
echo    - Предупреждает о классах, не описанных в документации
echo.
echo 🔧 Для установки markdownlint (опционально):
echo    npm install -g markdownlint-cli
echo.
echo 📚 Документация по правилам: ARCHITECTURE_RULES.md
echo.
echo ⚠️  Примечание: Для работы в Windows может потребоваться Git Bash или WSL
pause
