# ADR 0001: Модульный монолит

## Статус

Принято.

## Контекст

ATLAS должен оставаться простым для локального запуска, self-hosted использования и последовательного развития Telegram UX. При этом проекту нужны более чёткие границы между identity, Telegram adapter, profile, tracking, planning, reporting, setup, runtime и safety.

## Решение

ATLAS развивается как модульный монолит: один deployable artifact, одна локальная среда запуска и явные внутренние bounded contexts.

## Причины

- простой локальный запуск;
- удобная self-hosted distribution;
- низкая операционная сложность;
- достаточный масштаб для текущей стадии продукта;
- возможность будущего выделения сервисов через уже оформленные module boundaries.

## Последствия

Команды и callbacks Telegram должны входить в application use cases через adapter layer. Модули не должны обращаться к infrastructure соседних модулей без явной необходимости.
