# ADR 0001: Modular Monolith

## Status

Accepted.

## Context

ATLAS must remain easy to run locally, self-host and evolve while keeping Telegram UX stable. The project also needs clearer boundaries between identity, Telegram adapter, profile, tracking, planning, reporting, setup, runtime and safety.

## Decision

ATLAS is built as a modular monolith: one deployable artifact, one local launch path and explicit internal bounded contexts.

## Rationale

- simpler local launch;
- easier self-hosted distribution;
- lower operational complexity;
- enough scale for the current product stage;
- future service extraction remains possible through module boundaries.

## Consequences

Telegram commands and callbacks should enter application use cases through the adapter layer. Modules should not access another module's infrastructure without a clear reason.
