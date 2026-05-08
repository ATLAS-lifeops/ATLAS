# Issues for v0.2.0

GitHub issue operations were attempted through MCP, but issue creation returned `403 Resource not accessible by personal access token`.

## Closed

### 1. Add Dockerfile for Spring Boot application
Status: closed
Summary: Added a multi-stage Java 21 Dockerfile for building and running the Spring Boot application as a non-root user.

### 2. Add docker-compose setup with PostgreSQL
Status: closed
Summary: Added Docker Compose services for the ATLAS application and PostgreSQL 16 with a persistent named volume and health checks.

### 3. Add environment configuration example
Status: closed
Summary: Updated `.env.example` with PostgreSQL, Spring datasource, and Telegram integration variables.

### 4. Add local development documentation
Status: closed
Summary: Updated README with Docker Compose startup, shutdown, logs, rebuild, cleanup, and local test commands.

### 5. Add health endpoint and readiness notes
Status: closed
Summary: Added Spring Boot Actuator health exposure and Docker Compose health checks for local readiness.

### 6. Add basic GitHub Actions workflow
Status: closed
Summary: Added a basic Maven CI workflow for push and pull request events using Java 21.

### 7. Prepare v0.2.0 changelog entry
Status: closed
Summary: Added a v0.2.0 changelog entry describing the local infrastructure baseline.
