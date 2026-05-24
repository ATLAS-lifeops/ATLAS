# Changelog

## v0.4.0

- Added Flyway-backed persistence for Telegram users, messages, check-ins and runtime settings
- Added first-run setup page and setup status endpoint
- Added Telegram token validation through `getMe`
- Added polling mode with persisted update offset and webhook removal
- Added runtime webhook registration and webhook secret validation from saved settings
- Persisted incoming/outgoing Telegram messages and `/checkin` activity
- Integrated `/report` with saved check-ins and Telegram activity
- Updated setup, deployment and repository cleanup documentation

## v0.3.3

- Added production Telegram webhook configuration
- Added Telegram webhook secret validation
- Added optional webhook registration on application startup
- Added production-safe Telegram update logging
- Added Telegram production deployment documentation
- Expanded Telegram production test coverage

## v0.3.2

- Removed obsolete frontend-related artifacts from the backend repository
- Removed stale frontend references from docs and CI
- Clarified backend-only scope of the ATLAS repository
- Reorganized repository documentation layout
- Updated roadmap presentation and repository maintenance notes

## v0.3.1

- Stabilized Telegram integration startup behavior
- Added configuration validation for enabled Telegram mode
- Added safe handling for unsupported Telegram updates
- Added robust Telegram message sending behavior
- Added long response splitting for Telegram replies
- Expanded Telegram integration test coverage
- Improved Telegram setup and troubleshooting documentation
- Centralized static Telegram reply templates

## v0.3.0

- Added initial Telegram integration baseline
- Added Telegram webhook receiver foundation
- Added ATLAS command routing through the backend orchestrator
- Added static Telegram reply templates
- Updated README with Telegram bot workflow documentation

## v0.2.0

- Added Dockerfile for the Spring Boot application
- Added Docker Compose setup for local development
- Added PostgreSQL service with persistent volume
- Added environment variable example file
- Added Telegram integration enable/disable configuration
- Added health check foundation
- Updated README with Docker workflow
