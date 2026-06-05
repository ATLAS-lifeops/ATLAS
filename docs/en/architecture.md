# ATLAS Architecture

ATLAS is intentionally built as a modular monolith. At this stage, this keeps local development and Docker Compose launch simple while preserving clear boundaries between domain areas.

ATLAS is a backend-first Telegram life operating system focused on state, focus, habits, planning, reflection and progress. Telegram is an interaction channel, not the product domain model.

## Why Not Microservices Yet

Microservices are not used at this stage because the product benefits more from:

- simple local launch for self-hosted usage;
- lower operational complexity;
- one transactional model for user flows;
- faster iteration without distributed contracts;
- clear internal boundaries that can be extracted later.

A modular monolith keeps one deployable artifact, but still requires discipline: modules communicate through application use cases, ports or internal events instead of direct access to another module's infrastructure.

## Bounded Contexts

- `identity` - Telegram users, language, user resolution and current user lookup.
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

## Dependency Direction

Each major module moves toward this structure:

```text
module/
  domain/
  application/
  infrastructure/
  api/
```

Layer rules:

- `domain` does not depend on Spring, JPA, Telegram DTOs, HTTP clients or infrastructure;
- `application` depends on domain and ports;
- `infrastructure` implements ports and may depend on Spring/JPA/HTTP;
- `api` adapts external input into application use cases;
- `shared` contains only small cross-cutting primitives.

## Ports and Adapters

The application layer expresses product scenarios through use cases and ports. Infrastructure implements ports through JPA, Telegram API or other external mechanisms. Telegram callbacks, commands and buttons map to internal actions and use cases.

This makes product scenarios testable without Telegram API and database dependencies, while also defining future service boundaries.

## Internal Events

Internal events are in-process only. They mark important product facts: language selected, user onboarded, check-in completed, habit tracked, evening reflection completed, weekly report built, Telegram panel rendered and setup completed.

This release does not introduce external brokers. Events are extension points and future extraction boundaries.

## Future Extraction Candidates

Potential future extraction candidates:

- `telegram-gateway-service`;
- `life-tracking-service`;
- `llm-agent-service`;
- `memory-service`;
- `reporting-service`.

Extraction should happen only after internal contracts, events and ports are stable.
