# ADR-0003: Rule Engine Evaluation Strategy

## Status

Accepted

---

## Context

The Rule Engine is responsible only for evaluating incoming `LogEvent` objects
against the configured rules.

The system must remain independent from Kafka, PostgreSQL and REST APIs while
keeping the evaluation logic simple and maintainable.

---

## Decision

The Rule Engine evaluates rules using an independent
`SlidingWindowCounter` instance for each configured rule.

Each counter stores the matching `LogEvent` objects inside a time-based
sliding window.

When a rule condition is satisfied, the Rule Engine delegates alert creation
to `AlertService`.

The Rule Engine does not directly interact with:

- Kafka
- PostgreSQL
- AlertRepository
- REST API

---

## Rationale

This design keeps the Rule Engine focused on a single responsibility:

- evaluate rules

Infrastructure concerns remain isolated inside their dedicated classes.

Using one sliding window per rule also simplifies the implementation and
avoids coupling between different rule types.

---

## Consequences

### Positive

- High cohesion
- Low coupling
- Easy unit testing
- Independent rule evaluation
- Clear separation between business logic and infrastructure

### Negative

- A separate counter instance is maintained for each configured rule.