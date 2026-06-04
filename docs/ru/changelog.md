# История изменений

## v0.5.3

### Добавлено
- Добавлены стандартные кнопки навигации: Назад, Меню, Отменить, Продолжить.
- Добавлена панель продолжения незавершённого сценария.
- Добавлены действия после завершения check-in, плана дня, привычек, вечерней рефлексии и отчёта.
- Добавлены улучшенные empty states для отчёта, плана дня и привычек.
- Добавлена улучшенная панель настроек и профиля.
- Добавлено подтверждение перезапуска onboarding.

### Изменено
- Улучшена консистентность команд и кнопок.
- Улучшена обработка устаревших и некорректных callback-кнопок.
- Документация реорганизована в `/docs/ru` и `/docs/en`.

### Исправлено
- Меню больше не должно случайно сбрасывать активный сценарий.
- Секреты Telegram не отображаются в настройках и профиле.

## v0.5.2

- Добавлен дружелюбный локальный запуск через `make start`.
- Добавлены безопасные локальные `.env` placeholders.
- Добавлены setup mode и preconfigured local bot mode.
- Добавлен безопасный setup status без Telegram secrets.

## v0.5.1

- Добавлен Telegram UX layer с inline-кнопками, callback routing, language-first onboarding и меню.

## v0.5.0

- Добавлены life onboarding, check-in, habits, evening reflection и weekly report flows.

## v0.4.0

- Добавлены PostgreSQL persistence, runtime settings, setup page, Telegram token validation, polling и webhook modes.

## v0.3.x

- Добавлена базовая Telegram-интеграция, webhook endpoint, безопасное логирование и backend-only структура.

## v0.2.0

- Добавлены Dockerfile, Docker Compose, PostgreSQL и health check foundation.
