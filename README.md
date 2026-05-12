<p align="center">
  <img src="docs/assets/logo.png" alt="ATLAS logo" width="160">
</p>

<h1 align="center">ATLAS</h1>

**ATLAS** is a backend-first Telegram system for rhythm, training, recovery, habits, nutrition and progress.

The project is designed as a multi-agent Telegram assistant: the user talks to one bot, while the backend routes requests to specialized ATLAS agents.

Frontend, landing page and web dashboard work are outside the core roadmap of this repository and may evolve separately later.

## Core Scope

- Telegram-first backend product
- Spring Boot application with PostgreSQL persistence
- agent orchestration for personal life operations
- safe Telegram command handling
- future LLM provider abstraction

## Agents

| Agent | Responsibility |
|---|---|
| ATLAS Core | orchestration and routing |
| ATLAS Coach | sport, workouts, training load |
| ATLAS Planner | day and week planning |
| ATLAS Recovery | sleep, fatigue, recovery |
| ATLAS Habits | habits, discipline, rhythm |
| ATLAS Fuel | nutrition support |
| ATLAS Report | weekly analytics and progress |

## Commands

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

## Stack

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Flyway
- JUnit 5
- Telegram Bot API

## Local Run

Run tests:

```bash
mvn test
```

Run locally with Telegram disabled:

```bash
ATLAS_TELEGRAM_ENABLED=false mvn spring-boot:run
```

Run with Docker Compose:

```bash
cp .env.example .env
docker compose up --build
```

Health endpoint:

```text
http://localhost:8080/actuator/health
```

Telegram webhook endpoint:

```text
POST /telegram/webhook
```

## Configuration

Telegram integration is disabled by default for local development.

Required variables for enabled Telegram mode:

```bash
ATLAS_TELEGRAM_ENABLED=true
ATLAS_TELEGRAM_BOT_TOKEN=<token>
ATLAS_TELEGRAM_BOT_USERNAME=<username>
```

Do not commit real credentials.

## Roadmap

Core product roadmap:

```text
v0.3.0 — real Telegram adapter
v0.3.1 — Telegram stabilization
v0.4.0 — persistence for users, messages, check-ins
v0.5.0 — onboarding + conversational flows
v0.6.0 — LLM abstraction
v0.6.1 — first real LLM provider integration
```

Maintenance:

```text
v0.3.2 — backend-only repository cleanup and documentation alignment
```

## Safety

ATLAS is not a doctor, dietitian or medical professional. It should not diagnose, prescribe treatment, recommend training through pain, promote extreme diets or ignore serious symptoms.

## Docs

- [Backend scope](docs/architecture/backend-scope.md)

## License

License will be defined later.
