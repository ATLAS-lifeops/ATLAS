<p align="center">
  <img src="docs/assets/logo.png" alt="ATLAS logo" width="160">
</p>

<h1 align="center">ATLAS</h1>

**ATLAS** — backend-first Telegram-система для режима, тренировок, восстановления, привычек, питания и прогресса.

Проект задуман как мультиагентный Telegram-ассистент: пользователь общается с одним ботом, а backend маршрутизирует запросы к специализированным агентам ATLAS.

Фронтенд, лендинг и веб-кабинет не входят в основную дорожную карту этого репозитория и могут развиваться отдельно позже.

<h2 align="center">Core Scope</h2>

- Telegram-first backend-продукт
- Spring Boot приложение с хранением данных в PostgreSQL
- оркестрация агентов для повседневных сценариев пользователя
- безопасная обработка Telegram-команд
- будущая абстракция LLM-провайдера

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
/food
/report
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

Запуск через Docker Compose:

```bash
cp .env.example .env
docker compose up --build
```

Эндпоинт состояния:

```text
http://localhost:8080/actuator/health
```

Эндпоинт Telegram webhook:

```text
POST /telegram/webhook
```

<h2 align="center">Configuration</h2>

Telegram-интеграция по умолчанию выключена для локальной разработки.

Переменные, необходимые для включённого Telegram-режима:

```bash
ATLAS_TELEGRAM_ENABLED=true
ATLAS_TELEGRAM_BOT_TOKEN=<token>
ATLAS_TELEGRAM_BOT_USERNAME=<username>
```

Не добавляй реальные секреты в репозиторий.

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

<h2 align="center">License</h2>

Лицензия будет определена позже.
