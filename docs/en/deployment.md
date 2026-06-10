# Deployment Modes

ATLAS supports two deployment modes through `ATLAS_DEPLOYMENT_MODE`:

- `self_hosted`: default mode. Local setup can be enabled and a user-provided Telegram bot token is allowed.
- `hosted`: foundation for a server-owned runtime. Setup must be disabled, Telegram must run in webhook mode, and bot credentials must come from server environment or secrets.

Hosted mode blocks unsafe combinations such as public setup, missing webhook URL, missing webhook secret, missing bot token or polling-only runtime.

Status output may show mode, setup state, provider names and whether settings exist. It must not show Telegram tokens, webhook secrets or LLM API keys.

## Backup And Restore Checklist

- Back up PostgreSQL with `pg_dump` or provider-native snapshots.
- Back up persistent runtime volumes, especially `/app/data/memory` if memory snapshots are enabled.
- Do not back up plaintext `.env` files into shared storage; store secrets in the deployment secret manager.
- Restore PostgreSQL first, then runtime volumes, then restart ATLAS with the same `ATLAS_DEPLOYMENT_MODE`.
- Verify `/actuator/health/readiness` and `/actuator/health/liveness` after restore.
