# Admin Panel - NaidiZakupku Telegram

Административная панель для управления Telegram ботом.

## Технологический стек

- **React 18** - UI библиотека
- **TypeScript** - типизация
- **Vite** - сборщик и dev server
- **React Router v6** - маршрутизация
- **TanStack Query** - управление состоянием сервера
- **Zustand** - управление глобальным состоянием
- **Axios** - HTTP клиент
- **React Hook Form + Zod** - формы и валидация
- **Recharts** - графики
- **Ant Design** - UI компоненты
- **Day.js** - работа с датами

## Установка

```bash
cd admin-panel
npm install
```

## Настройка

Создайте файл `.env` в корне проекта `admin-panel/`:

```env
VITE_API_URL=http://localhost:8080
```

## Запуск в режиме разработки

```bash
npm run dev
```

Приложение будет доступно по адресу http://localhost:3000

Vite автоматически проксирует запросы к `/api` на backend (http://localhost:8080)

## Сборка для production

```bash
npm run build
```

Собранные файлы будут в папке `dist`

## Линтинг

```bash
npm run lint
```

## Структура проекта

```
admin-panel/
├── src/
│   ├── api/              # API клиенты
│   ├── components/       # Переиспользуемые компоненты
│   ├── pages/            # Страницы приложения
│   ├── hooks/            # Custom hooks
│   ├── store/            # Zustand stores
│   ├── types/            # TypeScript типы
│   ├── utils/            # Утилиты
│   ├── App.tsx           # Главный компонент
│   └── main.tsx          # Точка входа
├── public/               # Статические файлы
├── index.html            # HTML шаблон
├── package.json          # Зависимости
├── tsconfig.json         # Конфигурация TypeScript
└── vite.config.ts        # Конфигурация Vite
```

## Переменные окружения

Создайте файл `.env` в корне проекта:

```env
VITE_API_URL=http://localhost:8080
```

## API

Админка подключается к backend API по адресу `/api/admin/*`

Все endpoints требуют JWT аутентификации (кроме `/api/admin/auth/**`)

