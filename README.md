<p align="center">
  <img src="docs/assets/logo.png" alt="ATLAS logo" width="160">
</p>

<h1 align="center">ATLAS</h1>

**ATLAS** — backend-first Telegram-система для состояния, фокуса, привычек, планирования, рефлексии и прогресса.

Проект задуман как мультиагентный Telegram-ассистент: пользователь общается с одним ботом, а backend маршрутизирует запросы к специализированным агентам ATLAS.

Фронтенд, лендинг и веб-кабинет не входят в основную дорожную карту этого репозитория и могут развиваться отдельно позже.

<h2 align="center">Core Scope</h2>

- Telegram-first backend-продукт
- Spring Boot приложение с хранением данных в PostgreSQL
- оркестрация агентов для повседневных сценариев пользователя
- безопасная обработка Telegram-команд

<h2 align="center">Agents</h2>

| Агент | Ответственность |
|---|---|
| ATLAS Core | оркестрация и маршрутизация |
| ATLAS Coach | спорт, тренировки, нагрузка |
| ATLAS Planner | планирование дня и недели |
| ATLAS Recovery | сон, усталость, восстановление |
| ATLAS Habits | привычки, дисциплина, ритм |
| ATLAS Fuel | поддержка питания |
| ATLAS Report | недельная аналитика и прогресс |

<h2 align="center">Commands</h2>

```text
/start
/day
/week
/workout
/checkin
/recovery
/habits
/evening
/review
/food
/report
/cancel
/help
/emergency
```

<h2 align="center">Stack</h2>

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Flyway
- JUnit 5
- Telegram Bot API

<h2 align="center">Local Run</h2>

Запуск тестов:

```bash
mvn test
```

Локальный запуск с выключенной Telegram-интеграцией:

```bash
ATLAS_TELEGRAM_ENABLED=false mvn spring-boot:run
```

Рекомендуемый запуск для первого локального старта:

```bash
git clone https://github.com/ATLAS-lifeops/ATLAS.git
cd ATLAS
make start
```

Браузер откроется автоматически:

```text
http://localhost:8080/setup
```

`make start` запускает Docker Compose, ждёт готовности backend и открывает страницу настройки на стороне хоста. Это нужно потому, что `docker compose up -d` запускает контейнеры в фоне и не может надёжно открыть браузер хост-машины на macOS, Windows и Linux.

Прямой запуск через Docker Compose:

```bash
cp .env.example .env
docker compose up --build -d
```

После прямого запуска открой вручную:

```text
http://localhost:8080/setup
```

Дополнительные команды:

```bash
make up
make down
make logs
make restart
```

Эндпоинт состояния:

```text
http://localhost:8080/actuator/health
```

Первичная настройка после локального запуска:

```text
http://localhost:8080/setup
```

Эндпоинт Telegram webhook:

```text
POST /telegram/webhook
```

<h2 align="center">Configuration</h2>

Telegram-интеграция по умолчанию выключена для локальной разработки. При `ATLAS_SETUP_ENABLED=true` приложение может стартовать без bot token и принять настройки через `/setup`.

Минимальные переменные для первого запуска с web setup:

```bash
ATLAS_SETUP_ENABLED=true
ATLAS_TELEGRAM_ENABLED=false
```

Переменные для запуска только через окружение:

```bash
ATLAS_TELEGRAM_ENABLED=true
ATLAS_TELEGRAM_BOT_TOKEN=<token>
ATLAS_TELEGRAM_BOT_USERNAME=<username>
```

Режимы запуска Telegram:

```text
POLLING — простой локальный режим, приложение само читает getUpdates и удаляет активный webhook.
WEBHOOK — production-режим, приложение принимает POST /telegram/webhook и проверяет secret token.
```

Данные v0.5.0 хранятся в PostgreSQL через Flyway: runtime settings, Telegram users, Telegram messages, life profiles, conversation states, check-ins, habits и evening reflections. Команда `/report` использует сохранённые данные за последние 7 дней, если они есть.

Не добавляй реальные секреты в репозиторий.

<h2 align="center">Production Telegram Launch</h2>

Production-запуск Telegram-бота описан в [deployment guide](docs/deployment/telegram-production.md).

Минимальные production-переменные:

```bash
ATLAS_TELEGRAM_ENABLED=true
ATLAS_TELEGRAM_BOT_TOKEN=<telegram-bot-token>
ATLAS_TELEGRAM_BOT_USERNAME=<telegram-bot-username>
ATLAS_TELEGRAM_WEBHOOK_PATH=/telegram/webhook
ATLAS_TELEGRAM_WEBHOOK_SECRET=<random-webhook-secret>
ATLAS_PUBLIC_BASE_URL=https://<public-domain>
ATLAS_TELEGRAM_REGISTER_WEBHOOK_ON_STARTUP=true
```

Production health endpoint:

```text
GET /actuator/health
```

Telegram webhook endpoint:

```text
POST /telegram/webhook
```

<h2 align="center">Roadmap</h2>

Основная продуктовая дорожная карта:

```text
v0.3.0 — реальный Telegram-адаптер
v0.3.1 — стабилизация Telegram-интеграции
v0.4.0 — хранение данных пользователей, сообщений и чек-инов
v0.5.0 — онбординг и диалоговые сценарии
v0.6.0 — LLM-абстракция
v0.6.1 — первая интеграция реального LLM-провайдера
```

Служебные релизы:

```text
v0.3.2 — очистка backend-only репозитория и выравнивание документации
```

<h2 align="center">Safety</h2>

ATLAS не является врачом, диетологом или медицинским специалистом. Система не должна ставить диагнозы, назначать лечение, рекомендовать тренироваться через боль, продвигать экстремальные диеты или игнорировать серьёзные симптомы.

<h2 align="center">Docs</h2>

- [Границы backend-части](docs/architecture/backend-scope.md)
- [Life onboarding and tracking flows](docs/product/life-flows.md)
- [Telegram button UX](docs/product/telegram-ux.md)
- [Production Telegram запуск](docs/deployment/telegram-production.md)

<h2 align="center">License</h2>

Лицензия будет определена позже.
