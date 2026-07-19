# ADR-0001: Initial Architecture

## Status

Accepted

---

## Context

The goal of this project is to build a distributed log monitoring system within a limited development timeline while keeping the implementation simple, maintainable, and aligned with clean software engineering principles.

---

## Decision

The project adopts a Maven Multi-Module architecture consisting of:

- shared
- file-ingestor
- rules-evaluator

The architecture separates responsibilities between log ingestion and rule evaluation while sharing common domain models through a dedicated shared module.

The implementation intentionally avoids unnecessary architectural complexity such as Hexagonal Architecture, CQRS, and Event Sourcing.

---

## Rationale

This approach provides:

- Clear separation of responsibilities
- Low coupling between modules
- Simple project structure
- Good maintainability
- Fast development suitable for the project scope

---

## Consequences

### Positive

- Simple and understandable architecture
- Easy module isolation
- Reusable shared domain model
- Reduced project complexity

### Negative

- Future architectural changes may require additional refactoring if the project grows significantly.