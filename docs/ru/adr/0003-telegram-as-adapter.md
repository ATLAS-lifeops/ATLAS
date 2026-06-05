# ADR 0003: Telegram как adapter

## Статус

Принято.

## Контекст

ATLAS работает через Telegram, но продуктовая логика относится к состоянию, фокусу, привычкам, планированию, рефлексии и прогрессу пользователя.

## Решение

Telegram рассматривается как adapter, а не как domain. Telegram commands, callbacks, buttons и panels преобразуются во внутренние actions и application use cases.

## Причины

- логика ATLAS не должна зависеть от Telegram API DTO;
- callbacks и buttons должны мапиться на внутренние product actions;
- профиль, tracking, planning и reporting остаются переносимыми;
- будущий Telegram gateway можно выделить без переписывания domain logic.

## Последствия

Telegram layer отвечает за transport, rendering и mapping. Product behavior должен находиться в application services/use cases.
