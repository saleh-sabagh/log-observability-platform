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

The project follows constructor-based dependency injection without relying on a dependency injection framework.

Application configuration is loaded during application bootstrap and injected into infrastructure components instead of allowing each component to read configuration files independently.

Each module manages its own resources using the standard Maven `src/main/resources` directory.

The application entry point is responsible only for bootstrapping the application and wiring dependencies, while business logic remains inside dedicated components.

---

## Rationale

This approach provides:

- Clear separation of responsibilities
- Low coupling between modules
- Simple project structure
- Good maintainability
- Fast development suitable for the project scope
- Better testability through explicit dependency injection
- Centralized configuration management
- Standard Maven project layout
- Improved separation between application bootstrap and business logic

---

## Consequences

### Positive

- Simple and understandable architecture
- Easy module isolation
- Reusable shared domain model
- Reduced project complexity
- Components become easier to unit test
- Configuration is loaded only once during application startup
- Modules remain independent and self-contained
- Resources are packaged automatically by Maven
- Infrastructure components focus on a single responsibility

### Negative

- Manual dependency wiring is required in the application entry point.
- A future application module may become responsible for coordinating configuration across multiple modules.