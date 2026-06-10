# Режимы развёртывания

ATLAS поддерживает два режима через `ATLAS_DEPLOYMENT_MODE`:

- `self_hosted`: режим по умолчанию. Локальный setup может быть включён, Telegram bot token задаёт владелец установки.
- `hosted`: серверный режим. Setup должен быть выключен, Telegram работает через webhook, bot token и webhook secret берутся только из окружения или secret manager.

Hosted mode блокирует небезопасные сочетания: публичный setup, отсутствие webhook URL, отсутствие webhook secret, отсутствие bot token или polling-only runtime.

Статусы могут показывать режим, состояние setup и наличие настроек. Telegram token, webhook secret и LLM API key не отображаются.

## Backup And Restore Checklist

- Делай backup PostgreSQL через `pg_dump` или snapshots провайдера.
- Делай backup runtime volumes, особенно `/app/data/memory`, если включены memory snapshots.
- Не складывай plaintext `.env` в общий backup; секреты должны жить в secret manager.
- При восстановлении сначала восстанови PostgreSQL, затем runtime volumes, затем запусти ATLAS с тем же `ATLAS_DEPLOYMENT_MODE`.
- После восстановления проверь `/actuator/health/readiness` и `/actuator/health/liveness`.
