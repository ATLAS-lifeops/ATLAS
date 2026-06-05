# ADR 0003: Telegram as an Adapter

## Status

Accepted.

## Context

ATLAS operates through Telegram, but the product logic is about user state, focus, habits, planning, reflection and progress.

## Decision

Telegram is treated as an adapter, not the domain. Telegram commands, callbacks, buttons and panels map to internal actions and application use cases.

## Rationale

- ATLAS product logic should not depend on Telegram API DTOs;
- callbacks and buttons should map to internal product actions;
- profile, tracking, planning and reporting remain portable;
- a future Telegram gateway can be extracted without rewriting domain logic.

## Consequences

The Telegram layer owns transport, rendering and mapping. Product behavior belongs in application services and use cases.
