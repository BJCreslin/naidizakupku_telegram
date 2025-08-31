@echo off
REM Скрипт для исправления проблемы с JAXB в Java 21

echo 🔧 Исправление проблемы с JAXB в Java 21...

REM Проверяем, что мы в корневой директории проекта
if not exist "build.gradle.kts" (
    echo ❌ Ошибка: Запустите скрипт из корневой директории проекта
    exit /b 1
)

REM Очищаем и пересобираем проект
echo 📦 Очистка и пересборка проекта...
call gradlew clean build

if %ERRORLEVEL% EQU 0 (
    echo ✅ Проект успешно собран!
    echo.
    echo 🔍 Проверьте, что в build.gradle.kts добавлены зависимости:
    echo    implementation("javax.xml.bind:jaxb-api:2.3.1")
    echo    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.4")
    echo.
    echo 🚀 Теперь можно запускать приложение:
    echo    gradlew bootRun
) else (
    echo ❌ Ошибка при сборке проекта
    exit /b 1
)
