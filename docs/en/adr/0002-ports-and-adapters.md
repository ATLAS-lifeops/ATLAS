# ADR 0002: Ports and Adapters Inside Modules

## Status

Accepted.

## Context

ATLAS product logic should be testable and resilient to changes in Telegram API, JPA persistence and future external integrations.

## Decision

Major modules use ports and adapters: the application layer defines use cases and ports, while infrastructure implements those ports.

## Rationale

- domain logic stays independent from Telegram, JPA and future LLM integrations;
- scenarios are easier to test;
- future service boundaries become clearer;
- infrastructure details do not leak into product logic.

## Consequences

New scenarios should be added through application use cases. Infrastructure dependencies should remain in infrastructure or existing legacy services until they are gradually moved.
