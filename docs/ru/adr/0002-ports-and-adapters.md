# ADR 0002: Ports and adapters внутри модулей

## Статус

Принято.

## Контекст

Продуктовая логика ATLAS должна быть проверяемой и устойчивой к изменениям Telegram API, JPA persistence и будущих внешних интеграций.

## Решение

Крупные модули используют ports and adapters: application layer описывает use cases и ports, а infrastructure реализует эти ports.

## Причины

- domain остаётся независимым от Telegram, JPA и будущих LLM integrations;
- сценарии проще тестировать;
- границы будущих сервисов становятся понятнее;
- инфраструктурные детали не протекают в product logic.

## Последствия

Новые сценарии следует добавлять через application use cases. Инфраструктурные зависимости должны оставаться в infrastructure или существующих legacy services до их постепенного переноса.
