
# ADR-0002: Kafka Consumer Architecture

## Status
Accepted

## Context
The `rules-evaluator` module is responsible for consuming `LogEvent` messages from Apache Kafka and passing them to the rule evaluation pipeline.

To keep the architecture modular, testable, and aligned with the Single Responsibility Principle (SRP), the responsibilities of configuring Kafka, deserializing messages, consuming records, and processing business logic must remain clearly separated.

The project is implemented as a plain Java application without Spring Boot or any dependency injection framework. Therefore, all dependencies are created and wired manually.

## Decision
The Kafka consumer subsystem is divided into three independent components:

### 1. `KafkaConsumerConfig`
Responsible only for creating and configuring a `KafkaConsumer<String, LogEvent>` instance.
Responsibilities include:
- Loading Kafka consumer configuration from `config.properties`
- Configuring bootstrap servers
- Configuring consumer group ID
- Registering the key and value deserializers
- Disabling automatic offset commits
- Creating and returning a configured `KafkaConsumer`

*This class does not:*
- Subscribe to topics
- Poll records
- Process messages
- Execute business logic

### 2. `LogEventDeserializer`
A custom Kafka deserializer is introduced to convert Kafka message payloads (`byte[]`) into `LogEvent` objects using Jackson.
Responsibilities include:
- Deserialize JSON into `LogEvent`
- Return `null` for empty payloads
- Throw an exception when deserialization fails

The deserializer performs no validation or business processing.
Because `LogEvent` contains a `LocalDateTime` field, Jackson's `JavaTimeModule` is registered to ensure correct serialization and deserialization of Java Time types.

### 3. `KafkaLogConsumer`
`KafkaLogConsumer` is responsible for runtime message consumption.
Responsibilities include:
- Subscribing to Kafka topics
- Polling records
- Passing events to the `RuleEngine`
- Committing offsets only after successful processing

### Offset Management
Automatic offset commits are disabled:
```properties
enable.auto.commit=false

```

Offsets are committed manually only after the complete processing pipeline succeeds:

```text
Kafka
  ↓
KafkaLogConsumer
  ↓
RuleEngine
  ↓
AlertService
  ↓
AlertRepository (PostgreSQL)
  ↓
commitSync()

```

This approach prevents message loss if the application crashes after receiving a message but before it has been fully processed and stored.

## Consequences

### Advantages

* Clear separation of responsibilities.
* Easier unit testing.
* Manual dependency injection remains simple.
* Consumer configuration is reusable.
* Business logic is completely isolated from Kafka infrastructure.
* Reliable offset management reduces the risk of data loss.
* Consistent JSON handling through a custom serializer/deserializer pair.

### Trade-offs

* Slightly more classes compared to embedding everything inside the consumer.
* Manual dependency wiring is required because no DI framework is used.

However, the improved maintainability, testability, and architectural clarity outweigh the additional boilerplate.

