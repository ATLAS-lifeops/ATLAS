# Changelog

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
