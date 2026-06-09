# История изменений

## v0.9.0

### Добавлено
- Добавлены порты интеграций и безопасные metadata для настроек интеграций.
- Добавлена основа пользовательского Markdown export.
- Добавлен контракт preview для calendar integration без OAuth и внешней синхронизации.

## v0.8.2

### Добавлено
- Добавлен детерминированный trend detection для энергии, фокуса, стресса и сна.
- Добавлен анализ регулярности привычек.
- Добавлена основа архива отчетов.

## v0.8.1

### Добавлено
- Добавлена модель фокуса недели.
- Добавлен сервис недельного планирования для сохранения и получения текущего фокуса.
- Подготовлена связь недельного плана с недельным отчетом.

## v0.8.0

### Добавлено
- Добавлены настройки рутины: check-in time, evening time, timezone, quiet hours и enabled state.
- Добавлена основа scheduler, которая учитывает quiet hours.

## v0.7.3

### Добавлено
- Добавлены основы hosted rate limits и LLM quotas.
- Readiness и liveness остаются доступны через Spring Boot health probes.

## v0.7.2

### Добавлено
- Добавлены основы privacy panel, export, forget memory и delete my data.
- Добавлены строгие подтверждения для разрушающих операций.

### Безопасность
- Privacy operations работают только в рамках пользователя и не раскрывают секреты.

## v0.7.1

### Добавлено
- Добавлена основа hosted runtime для серверной Telegram-конфигурации.
- Добавлены webhook-first проверки через deployment validation.
- Добавлен базовый per-user rate limiting.

## v0.7.0

### Добавлено
- Добавлены явные режимы self-hosted и hosted.
- Добавлены безопасный deployment status и валидация небезопасных hosted-сочетаний.

## v0.6.4

### Добавлено
- Добавлен memory-aware LLM context assembly.
- Добавлены лимиты контекста и user-scoped retrieval для shared и agent-specific memory.

## v0.6.3

### Добавлено
- Добавлена PostgreSQL-схема для memory records.
- Добавлены user-scoped repository и persistent memory service.
- Добавлены опциональные runtime Markdown memory snapshots.

## v0.6.2

### Добавлено
- Добавлены memory write model, policy, validation result и memory service contract.
- Agent results расширены proposed memory writes.

## v0.6.1

### Добавлено
- Добавлены первые scoped LLM agent abstractions и question agent.
- Добавлены fallback и safety metadata для agent responses.

## v0.6.0

### Добавлено
- Добавлен опциональный LLM abstraction layer.
- Добавлен OpenAI-compatible LLM client.
- Добавлены настройки LLM через environment variables.
- Добавлены prompt templates для плана дня, недельного отчёта и вопросов.
- Добавлен сбор контекста из профиля, check-in, привычек и рефлексий.
- Добавлено LLM-улучшение плана дня при включённом LLM.
- Добавлено LLM-улучшение недельного отчёта при включённом LLM.
- Добавлен структурированный режим ответов на вопросы в рамках ATLAS.
- Добавлен безопасный fallback на deterministic responses.
- Добавлена документация по LLM на русском и английском языках.

### Изменено
- ATLAS может работать как с LLM, так и без LLM.
- В setup/status добавлен безопасный статус LLM без отображения секретов.

### Исправлено
- Добавлена защита от утечки LLM API key в логах, UI и документации.
- Добавлена обработка timeout, rate limit и ошибок провайдера.

### Безопасность
- LLM не используется для медицинских диагнозов или лечебных рекомендаций.
- При серьёзных симптомах ATLAS использует безопасные ответы и рекомендует обратиться к специалисту.

## v0.5.4

### Добавлено
- Добавлена документация по архитектуре модульного монолита.
- Добавлены ADR по ключевым архитектурным решениям.
- Добавлены внутренние application events.
- Добавлены архитектурные тесты для контроля зависимостей между слоями.

### Изменено
- Уточнены границы модулей: Telegram, identity, setup, profile, tracking, planning, reporting, safety и runtime.
- Telegram слой оформлен как adapter, а не как доменная логика.
- Основные сценарии перенесены ближе к application use cases.
- README дополнен архитектурным описанием проекта.

### Архитектура
- ATLAS остаётся модульным монолитом.
- Микросервисы не вводятся в этом релизе.
- Зафиксирован будущий путь выделения сервисов через bounded contexts и internal events.

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
