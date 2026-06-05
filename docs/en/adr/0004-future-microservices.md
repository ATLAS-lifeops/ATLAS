# ADR 0004: Future Microservices

## Status

Accepted.

## Context

Some ATLAS areas may eventually become separate services, but premature splitting would complicate local launch, deployment and diagnostics.

## Decision

Microservices are not extracted in this release. Future candidates are defined through bounded contexts, ports and internal events.

## Rationale

- avoid premature distributed complexity;
- preserve simple Docker Compose launch;
- avoid brokers and network contracts before they are needed;
- define future extraction candidates through module boundaries and events.

## Consequences

Potential candidates are `telegram-gateway-service`, `life-tracking-service`, `llm-agent-service`, `memory-service` and `reporting-service`. Extraction depends on stable internal contracts and real operational need.
