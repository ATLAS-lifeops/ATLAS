# Changelog

## Unreleased

### Changed
- Connected user-aware agent routing so Planner, Report and Question agents can use persisted user context.
- Persisted agent-proposed memory writes through `AgentMemoryService`.
- Wired privacy commands to real export, forget-memory and delete-my-data service behavior.
- Connected weekly focus to weekly reports and archived generated reports.
- Persisted integration settings through `IntegrationSettingsPort`.
- Wired hosted rate limits and LLM quotas into Telegram and LLM flows.
- Hardened hosted mode to require a webhook secret and documented backup/restore.

## v0.9.0

### Added
- Added integration port interfaces and safe integration settings metadata.
- Added user-scoped Markdown export foundation.
- Added calendar integration preview contract without OAuth or external sync.

## v0.8.2

### Added
- Added deterministic trend detection for energy, focus, stress and sleep.
- Added habit consistency analysis.
- Added report archive persistence foundation.

## v0.8.1

### Added
- Added persisted weekly focus model.
- Added weekly planning service for saving and retrieving current focus.
- Prepared weekly report connection points for weekly plan data.

## v0.8.0

### Added
- Added routine preferences for check-in time, evening time, timezone, quiet hours and enabled state.
- Added reminder scheduler foundation that respects quiet hours.

## v0.7.3

### Added
- Added hosted rate-limit and LLM quota foundations.
- Kept health endpoints available through Spring Boot readiness and liveness probes.

## v0.7.2

### Added
- Added privacy panel, export, forget-memory and delete-my-data service foundations.
- Added strong confirmation checks for destructive operations.

### Security
- Privacy operations are user-scoped and do not expose raw secrets.

## v0.7.1

### Added
- Added hosted runtime foundations for server-owned Telegram configuration.
- Added webhook-first safety checks through deployment validation.
- Added basic per-user rate limiting.

## v0.7.0

### Added
- Added explicit self-hosted and hosted deployment modes.
- Added safe deployment status and validation for unsafe hosted combinations.

## v0.6.4

### Added
- Added memory-aware LLM context assembly.
- Added context limits and user-scoped shared/agent-specific memory retrieval.

## v0.6.3

### Added
- Added PostgreSQL persistence schema for memory records.
- Added user-scoped memory repository and persistent memory service.
- Added optional runtime Markdown memory snapshots.

## v0.6.2

### Added
- Added memory write model, policy, validation result and memory service contract.
- Extended agent results with proposed memory writes.

## v0.6.1

### Added
- Added first scoped LLM agent abstractions and question agent.
- Added fallback and safety metadata on agent responses.

## v0.6.0

### Added
- Added optional LLM abstraction layer.
- Added OpenAI-compatible LLM client.
- Added LLM configuration through environment variables.
- Added prompt templates for day plans, weekly reports and questions.
- Added context assembly from profile, check-ins, habits and reflections.
- Added optional LLM-enhanced day plans.
- Added optional LLM-enhanced weekly report summaries.
- Added structured question answering within ATLAS scope.
- Added deterministic fallback behavior.
- Added Russian and English LLM documentation.

### Changed
- ATLAS can run with or without LLM.
- Setup/status now includes safe LLM status without exposing secrets.

### Fixed
- Prevented LLM API key leakage in logs, UI and documentation.
- Added handling for timeout, rate limit and provider errors.

### Security
- LLM is not used for diagnosis or treatment recommendations.
- For serious symptoms, ATLAS uses safe responses and recommends contacting a qualified professional.

## v0.5.4

### Added
- Added modular monolith architecture documentation.
- Added ADRs for key architecture decisions.
- Added internal application events.
- Added architecture tests for dependency boundaries.

### Changed
- Clarified module boundaries: Telegram, identity, setup, profile, tracking, planning, reporting, safety and runtime.
- Framed Telegram as an adapter rather than domain logic.
- Moved core scenarios closer to application use cases.
- Added architecture summary to README.

### Architecture
- ATLAS remains a modular monolith.
- Microservices are not introduced in this release.
- Future extraction paths are documented through bounded contexts and internal events.

## v0.5.3

### Added
- Added standard navigation buttons: Back, Menu, Cancel and Continue.
- Added unfinished flow continuation panel.
- Added next-action buttons after check-in, day plan, habits, evening reflection and reports.
- Added improved empty states for reports, day plans and habits.
- Added improved Settings and Profile panels.
- Added restart onboarding confirmation.

### Changed
- Improved consistency between commands and buttons.
- Improved handling for stale and malformed callback buttons.
- Reorganized documentation into `/docs/ru` and `/docs/en`.

### Fixed
- Menu should no longer silently discard active flows.
- Telegram secrets are not shown in settings or profile panels.

## v0.5.2

- Added friendly local launch through `make start`.
- Added safe local `.env` placeholders.
- Added setup mode and preconfigured local bot mode.
- Added safe setup status without Telegram secrets.

## v0.5.1

- Added Telegram UX layer with inline buttons, callback routing, language-first onboarding and menu.

## v0.5.0

- Added life onboarding, check-in, habits, evening reflection and weekly report flows.

## v0.4.0

- Added PostgreSQL persistence, runtime settings, setup page, Telegram token validation, polling and webhook modes.

## v0.3.x

- Added baseline Telegram integration, webhook endpoint, safe logging and backend-only structure.

## v0.2.0

- Added Dockerfile, Docker Compose, PostgreSQL and health check foundation.
