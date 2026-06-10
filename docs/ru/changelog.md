# История изменений

## Unreleased

### Изменено
- Agent routing стал user-aware: Planner, Report и Question agents получают persisted user context.
- Agent-proposed memory writes сохраняются через `AgentMemoryService`.
- Команды `/privacy`, `/memory`, `/export`, `/forget DELETE` и `/delete_my_data DELETE` подключены к реальному сервисному поведению.
- Weekly focus подключён к weekly report, отчёты архивируются после генерации.
- Integration settings сохраняются через `IntegrationSettingsPort`.
- Hosted rate limits и LLM quotas подключены к Telegram и LLM flows.
- Hosted mode требует webhook secret; добавлен backup/restore checklist.

## v0.9.0

- Добавлены порты интеграций, settings model, Markdown export foundation и calendar preview contract.

## v0.8.2

- Добавлены deterministic trends, habit consistency и report archive foundation.

## v0.8.1

- Добавлены weekly focus model и weekly planning service.

## v0.8.0

- Добавлены routine preferences и scheduler foundation с quiet hours.

## v0.7.x

- Добавлены deployment modes, hosted foundation, privacy controls и hosted production hardening foundations.

## v0.6.x

- Добавлены LLM agents, memory contract, persistent memory и memory-aware context assembly.

## v0.5.x

- Добавлены life flows, Telegram UX, local launch и модульная архитектура.

## v0.4.0

- Добавлены PostgreSQL persistence, runtime settings, setup page, polling и webhook modes.

## v0.3.x

- Добавлена базовая Telegram-интеграция, webhook endpoint и безопасное логирование.

## v0.2.0

- Добавлены Dockerfile, Docker Compose, PostgreSQL и health check foundation.
