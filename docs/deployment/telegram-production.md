# Telegram Production Deployment

Этот чеклист описывает production-запуск ATLAS backend для Telegram-бота.

## BotFather Checklist

- Создай бота через BotFather.
- Сохрани bot token вне репозитория.
- Задай bot username и description.
- Отключи privacy mode только если это нужно для групповых чатов.
- Не публикуй token в README, логах, скриншотах или issue.

## Required Environment Variables

```bash
ATLAS_TELEGRAM_ENABLED=true
ATLAS_TELEGRAM_BOT_TOKEN=<telegram-bot-token>
ATLAS_TELEGRAM_BOT_USERNAME=<telegram-bot-username>
ATLAS_TELEGRAM_WEBHOOK_PATH=/telegram/webhook
ATLAS_TELEGRAM_WEBHOOK_SECRET=<random-webhook-secret>
ATLAS_PUBLIC_BASE_URL=https://<public-domain>
ATLAS_TELEGRAM_REGISTER_WEBHOOK_ON_STARTUP=false
ATLAS_TELEGRAM_DROP_PENDING_UPDATES_ON_WEBHOOK_REGISTRATION=true
```

## Example `.env.production`

```bash
POSTGRES_DB=atlas
POSTGRES_USER=atlas
POSTGRES_PASSWORD=<postgres-password>

SPRING_DATASOURCE_URL=jdbc:postgresql://atlas-postgres:5432/atlas
SPRING_DATASOURCE_USERNAME=atlas
SPRING_DATASOURCE_PASSWORD=<postgres-password>

ATLAS_TELEGRAM_ENABLED=true
ATLAS_TELEGRAM_BOT_TOKEN=<telegram-bot-token>
ATLAS_TELEGRAM_BOT_USERNAME=<telegram-bot-username>
ATLAS_TELEGRAM_WEBHOOK_PATH=/telegram/webhook
ATLAS_TELEGRAM_WEBHOOK_SECRET=<random-webhook-secret>
ATLAS_PUBLIC_BASE_URL=https://<public-domain>
ATLAS_TELEGRAM_REGISTER_WEBHOOK_ON_STARTUP=true
ATLAS_TELEGRAM_DROP_PENDING_UPDATES_ON_WEBHOOK_REGISTRATION=true
```

## HTTPS Requirement

Telegram must reach the webhook through a public HTTPS URL. `ATLAS_PUBLIC_BASE_URL` must start with `https://` when automatic webhook registration is enabled.

## Docker Deployment Checklist

- Build and publish the backend image.
- Provision PostgreSQL and set datasource environment variables.
- Set all Telegram variables in the deployment environment.
- Keep `ATLAS_TELEGRAM_ENABLED=false` for local development unless testing a real bot.
- Expose the application on port `8080` behind HTTPS.
- Verify health before registering the webhook.

Healthcheck:

```bash
curl -f https://<public-domain>/actuator/health
```

Local container health endpoint:

```bash
curl -f http://localhost:8080/actuator/health
```

## Webhook Registration

### Option 1: Automatic Registration On Startup

Set:

```bash
ATLAS_TELEGRAM_REGISTER_WEBHOOK_ON_STARTUP=true
ATLAS_PUBLIC_BASE_URL=https://<public-domain>
ATLAS_TELEGRAM_WEBHOOK_PATH=/telegram/webhook
```

On startup the backend registers:

```text
https://<public-domain>/telegram/webhook
```

Automatic registration fails fast if the public URL is missing, is not HTTPS, or the webhook path does not start with `/`.

### Option 2: Manual `setWebhook`

Keep automatic registration disabled:

```bash
ATLAS_TELEGRAM_REGISTER_WEBHOOK_ON_STARTUP=false
```

Register manually:

```bash
curl -X POST "https://api.telegram.org/bot${ATLAS_TELEGRAM_BOT_TOKEN}/setWebhook" \
  -d "url=${ATLAS_PUBLIC_BASE_URL}${ATLAS_TELEGRAM_WEBHOOK_PATH}" \
  -d "secret_token=${ATLAS_TELEGRAM_WEBHOOK_SECRET}" \
  -d "drop_pending_updates=${ATLAS_TELEGRAM_DROP_PENDING_UPDATES_ON_WEBHOOK_REGISTRATION}" \
  -d 'allowed_updates=["message"]'
```

Check webhook status:

```bash
curl "https://api.telegram.org/bot${ATLAS_TELEGRAM_BOT_TOKEN}/getWebhookInfo"
```

## Smoke Test

- `GET /actuator/health` returns `UP`.
- Application starts with `ATLAS_TELEGRAM_ENABLED=true` and a configured bot token.
- Startup logs show Telegram integration enabled.
- If automatic registration is enabled, startup logs show webhook registration success.
- Send `/start` to the bot in Telegram.
- Bot replies in Telegram.
- Send `/day` and verify a planner response.
- Send an unsupported or blank update through the webhook and verify the backend does not crash.
- Verify logs contain `update_id`, `chat_id`, `handled`, and `request_type` where available.
- Verify logs do not contain bot token, webhook secret, or full user message text.

## Common Issues

Bot does not answer:
Check `ATLAS_TELEGRAM_ENABLED`, bot token, webhook URL, app logs, and `getWebhookInfo`.

Health is down:
Check datasource variables, PostgreSQL availability, migrations, container logs, and `/actuator/health`.

Wrong webhook URL:
Verify `ATLAS_PUBLIC_BASE_URL` and `ATLAS_TELEGRAM_WEBHOOK_PATH`. The final URL must be reachable by Telegram over HTTPS.

Invalid token:
Regenerate the token in BotFather and update only the deployment secret store.

Webhook secret mismatch:
The value in `ATLAS_TELEGRAM_WEBHOOK_SECRET` must match the `secret_token` used in `setWebhook`.

App is running but Telegram cannot reach it:
Check DNS, HTTPS certificate, reverse proxy routing, firewall rules, and public access to the webhook path.

Pending updates after deploy:
Use `ATLAS_TELEGRAM_DROP_PENDING_UPDATES_ON_WEBHOOK_REGISTRATION=true` during registration when old updates should be discarded.

## Security Notes

- Never commit the bot token.
- Use `ATLAS_TELEGRAM_WEBHOOK_SECRET` in production.
- Use HTTPS for the public webhook URL.
- Keep Telegram disabled locally by default.
- Store production secrets in the deployment secret manager or environment, not in git.
