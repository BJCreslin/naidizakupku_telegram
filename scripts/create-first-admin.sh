#!/bin/bash

# Скрипт для создания первого администратора
# Использование: ./scripts/create-first-admin.sh <username> <password> [email]

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Проверка аргументов
if [ $# -lt 2 ]; then
    echo -e "${RED}Ошибка: Недостаточно аргументов${NC}"
    echo "Использование: $0 <username> <password> [email]"
    echo "Пример: $0 admin mypassword123 admin@example.com"
    exit 1
fi

USERNAME=$1
PASSWORD=$2
EMAIL=${3:-""}

# URL API (можно переопределить через переменную окружения)
API_URL=${API_URL:-"http://localhost:8080"}

echo -e "${YELLOW}Создание первого администратора...${NC}"
echo "Username: $USERNAME"
if [ -n "$EMAIL" ]; then
    echo "Email: $EMAIL"
fi
echo "API URL: $API_URL"
echo ""

# Проверяем, есть ли уже администраторы
echo "Проверка наличия администраторов..."
HAS_ADMINS=$(curl -s "$API_URL/api/admin/auth/has-admins" | grep -o '"hasAdmins":[^,}]*' | cut -d':' -f2)

if [ "$HAS_ADMINS" = "true" ]; then
    echo -e "${RED}Ошибка: В системе уже есть администраторы${NC}"
    echo "Используйте обычную регистрацию или обратитесь к существующему администратору"
    exit 1
fi

# Создаем JSON payload
if [ -n "$EMAIL" ]; then
    JSON_PAYLOAD=$(cat <<EOF
{
  "username": "$USERNAME",
  "password": "$PASSWORD",
  "email": "$EMAIL"
}
EOF
)
else
    JSON_PAYLOAD=$(cat <<EOF
{
  "username": "$USERNAME",
  "password": "$PASSWORD"
}
EOF
)
fi

# Отправляем запрос
echo "Отправка запроса на создание администратора..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/api/admin/auth/register-first-admin" \
    -H "Content-Type: application/json" \
    -d "$JSON_PAYLOAD")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    echo -e "${GREEN}✓ Администратор успешно создан!${NC}"
    echo ""
    echo "Детали:"
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
    echo ""
    echo -e "${GREEN}Теперь вы можете войти в админку используя:${NC}"
    echo "  Username: $USERNAME"
    echo "  Password: $PASSWORD"
    echo ""
    echo "Админка доступна по адресу: $API_URL/admin"
else
    echo -e "${RED}✗ Ошибка при создании администратора${NC}"
    echo "HTTP код: $HTTP_CODE"
    echo "Ответ: $BODY"
    exit 1
fi

