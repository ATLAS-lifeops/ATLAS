# Архитектура ATLAS

ATLAS намеренно развивается как модульный монолит. На текущем этапе это даёт простую локальную разработку, предсказуемый запуск через Docker Compose и при этом сохраняет чёткие границы между доменными областями.

ATLAS - backend-first Telegram life operating system: система для состояния, фокуса, привычек, планирования, рефлексии и прогресса. Telegram является каналом взаимодействия, а не доменной моделью продукта.

## Почему не микросервисы

Микросервисы пока не используются, потому что текущей стадии продукта важнее:

- простой локальный запуск для self-hosted сценариев;
- минимальная операционная сложность;
- единая транзакционная модель для пользовательских сценариев;
- быстрые изменения без распределённых контрактов;
- понятные внутренние границы, которые можно выделить позже.

Модульный монолит сохраняет один deployable artifact, но требует дисциплины: модули должны общаться через application use cases, ports или internal events, а не через прямой доступ к инфраструктуре соседних модулей.

## Bounded contexts

- `identity` - Telegram users, language, user resolution, current user lookup.
- `telegram` - updates, callbacks, panels, keyboards and message delivery.
- `setup` - first-run setup, runtime configuration form and setup mode state.
- `profile` - life profile and onboarding.
- `tracking` - check-ins, habits and evening reflections.
- `planning` - day plan and minimal emergency plan.
- `reporting` - weekly reports and deterministic summaries.
- `safety` - safety guard and high-risk language handling.
- `runtime` - app status, local launch status and safe runtime settings.
- `shared` - IDs, clocks, errors and in-process events.

Future modules such as `llm` and `memory` can be introduced behind ports, but they are not part of this release scope.

## Направление зависимостей

Каждый крупный модуль движется к структуре:

```text
module/
  domain/
  application/
  infrastructure/
  api/
```

Правила слоёв:

- `domain` не зависит от Spring, JPA, Telegram DTO, HTTP clients или инфраструктуры;
- `application` зависит от domain и ports;
- `infrastructure` реализует ports и может зависеть от Spring/JPA/HTTP;
- `api` адаптирует внешний ввод в application use cases;
- `shared` содержит только небольшие общие примитивы.

## Ports and adapters

Application layer описывает сценарии через use cases и ports. Infrastructure реализует ports через JPA, Telegram API или другие внешние механизмы. Telegram callbacks, commands и buttons преобразуются во внутренние действия и use cases.

Такой подход позволяет тестировать сценарии без Telegram API и БД, а также заранее фиксирует границы для возможного выделения сервисов.

## Internal events

Internal events используются только внутри процесса. Они помогают отмечать важные продуктовые факты: выбран язык, пользователь завершил onboarding, сохранён check-in, привычка отмечена, рефлексия завершена, отчёт построен, Telegram panel отрисована, setup завершён.

В этом релизе события не отправляются во внешние брокеры. Они являются точками расширения и будущего извлечения сервисов.

## Future extraction candidates

Кандидаты на будущее выделение:

- `telegram-gateway-service`;
- `life-tracking-service`;
- `llm-agent-service`;
- `memory-service`;
- `reporting-service`.

Выделение возможно только после стабилизации внутренних контрактов, событий и ports.
