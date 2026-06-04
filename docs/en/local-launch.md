# Local Launch

Recommended first launch:

```bash
git clone https://github.com/ATLAS-lifeops/ATLAS.git
cd ATLAS
make start
```

`make start` starts Docker Compose, waits for `GET /actuator/health`, and opens setup:

```text
http://localhost:8080/setup
```

If the browser does not open automatically, open the URL manually.

## Modes

Setup mode:
1. Run `make start`.
2. Open `/setup`.
3. Paste the Telegram Bot Token.
4. Choose `Simple local launch`.
5. Save setup and send `/start` to the bot.

Preconfigured local bot mode:
1. Copy `.env.example` to `.env`.
2. Fill `ATLAS_TELEGRAM_BOT_TOKEN` locally only.
3. Run `make start`.

ATLAS validates the token through Telegram `getMe`, saves runtime settings, and starts polling.

## Commands

```bash
make start
make up
make down
make logs
make restart
make status
make clean
```

## Security

- Do not add `.env` to git.
- Do not publish Telegram Bot Token.
- Do not publish webhook secret.
- Runtime data stays in Docker volumes or local ignored directories.
