# Локальный запуск

Рекомендуемый первый запуск:

```bash
git clone https://github.com/ATLAS-lifeops/ATLAS.git
cd ATLAS
make start
```

`make start` запускает Docker Compose, ждёт `GET /actuator/health` и открывает страницу настройки:

```text
http://localhost:8080/setup
```

Если браузер не открылся автоматически, открой адрес вручную.

## Режимы

Setup mode:
1. Запусти `make start`.
2. Открой `/setup`.
3. Вставь Telegram Bot Token.
4. Выбери `Simple local launch`.
5. Сохрани настройки и напиши `/start` боту.

Preconfigured local bot mode:
1. Скопируй `.env.example` в `.env`.
2. Заполни `ATLAS_TELEGRAM_BOT_TOKEN` только локально.
3. Запусти `make start`.

ATLAS проверит token через Telegram `getMe`, сохранит runtime settings и запустит polling.

## Команды

```bash
make start
make up
make down
make logs
make restart
make status
make clean
```

## Безопасность

- Не добавляй `.env` в git.
- Не публикуй Telegram Bot Token.
- Не публикуй webhook secret.
- Runtime data хранится в Docker volumes или локальных ignored-директориях.
