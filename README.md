
# 📊 Log Monitoring System

A distributed, event-driven log monitoring and alerting system built with Java 21, Apache Kafka, and PostgreSQL.

This project was developed as a technical assessment to demonstrate solid software engineering principles, modular architecture, clean code practices, and scalable event-driven design.

---

## 🎯 Overview & Objectives

The system actively monitors application log files, parses raw log entries into structured domain events, and streams them through **Apache Kafka**. A dedicated rules engine evaluates these events in real-time using **sliding time windows**, persists generated alerts to **PostgreSQL**, and exposes them via a lightweight **REST API**.

**Key Features:**
* **Real-time File Ingestion:** Detects and parses new log entries on the fly.
* **Event-Driven Pipeline:** Decoupled architecture using Kafka for high throughput and fault tolerance.
* **Dynamic Rule Engine:** Supports diverse monitoring rules (e.g., `ERROR`, `LEVEL_RATE`, `TOTAL_RATE`) with sliding window evaluation to prevent alert storms.
* **Lightweight API:** Exposes alerts through a fast RESTful endpoint (powered by Javalin).
* **Defensive Programming:** Implements fail-fast mechanisms, connection pooling (HikariCP), and manual offset commits for at-least-once delivery guarantees.

---

## 🏗 Architecture & Workflow

The project follows a **Maven Multi-Module** architecture. It intentionally avoids unnecessary complexity (like Hexagonal Architecture or CQRS) where it doesn't add value, adhering to the **KISS** (Keep It Simple, Stupid) and **SRP** (Single Responsibility) principles.

```text
[ Log Files ] 
      │
      ▼
 📦 File Ingestor (Module 1)
   ├─ Watches log directories
   ├─ Parses text into LogEvents
   └─ Publishes to Kafka Topic
      │
      ▼
 🚀 Apache Kafka (Message Broker)
      │
      ▼
 ⚙️ Rules Evaluator (Module 2)
   ├─ Consumes LogEvents (Manual Commit)
   ├─ Evaluates via Sliding Window Counters
   ├─ Persists Alerts to Database
   └─ Serves REST API for Alerts
      │
      ▼
 🗄️ PostgreSQL (Storage) & 🌐 REST API (Consumers)

```

---

## 🛠 Technology Stack

| Component | Technology |
| --- | --- |
| **Core Language** | Java 21 |
| **Build Tool** | Maven (Multi-Module) |
| **Message Broker** | Apache Kafka |
| **Database** | PostgreSQL |
| **Connection Pool** | HikariCP |
| **REST Framework** | Javalin |
| **JSON Processing** | Jackson |
| **Infrastructure** | Docker & Docker Compose |

---

## 📁 Project Structure

```text
log-monitoring-system/
│
├── shared/               # Shared domain entities (e.g., LogEvent) to prevent code duplication.
├── file-ingestor/        # Watches files, parses logs, and produces Kafka messages.
├── rules-evaluator/      # Consumes Kafka, evaluates rules, saves to DB, and provides API.
├── config/               # Global configurations (rules.json, application.properties).
├── adr/                  # Architecture Decision Records explaining design choices.
└── docker-compose.yml    # Infrastructure setup for Kafka, Zookeeper, and PostgreSQL.

```

---

## 🚀 Quick Setup & Execution

### 1. Prerequisites

Ensure you have the following installed on your machine:

* **Java 21**
* **Maven 3.8+**
* **Docker & Docker Compose**

### 2. Start the Infrastructure

Spin up Apache Kafka and PostgreSQL using Docker:

```bash
git clone https://github.com/saleh-sabagh/log-observability-platform
cd log-monitoring-system

# Start Kafka and PostgreSQL in the background
docker compose up -d

# Verify containers are running
docker ps

```

### 3. Build the Project

Compile the project and resolve all Maven dependencies:

```bash
mvn clean install

```

### 4. Run the Application Modules

Since this is a distributed system, you need to run the two modules independently. You can run them via your IDE (by executing the `Main.java` class in each module) or using Maven:

**Terminal 1: Start the Rules Evaluator**
This module must start first to ensure the database tables are initialized and the Kafka consumer is ready.

```bash
cd rules-evaluator
mvn exec:java -Dexec.mainClass="Main"

```

**Terminal 2: Start the File Ingestor**
Once the evaluator is running, start the ingestor to begin reading log files and publishing them.

```bash
cd file-ingestor
mvn exec:java -Dexec.mainClass="Main"

```

---

## ⚙️ Configuration (`rules.json`)

Monitoring rules are dynamically loaded at startup from `config/rules.json`. The rules engine evaluates log frequencies using a thread-safe sliding window.

**Example Rule Configuration:**

```json
[
  {
    "ruleName": "High Error Rate",
    "ruleType": "LEVEL_RATE",
    "logLevel": "ERROR",
    "timeWindowSeconds": 60,
    "threshold": 10,
    "maxLastMessages": 3,
    "enabled": true
  }
]

```

*This rule generates an alert if 10 `ERROR` logs occur within any 60-second sliding window, attaching the last 3 log messages to the alert description.*

---

## 🔌 REST API Documentation

Once the `rules-evaluator` module is running, the API is available by default at `http://localhost:8081`.

### Get All Alerts

Retrieves a list of all triggered alerts, ordered by the most recent first.

**Request:**
`GET /alerts`

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "ruleName": "High Error Rate",
    "component": "payment-service",
    "description": "Rule 'High Error Rate' triggered with 10 matching event(s). Recent messages: DB timeout | Connection reset",
    "createdAt": "2026-07-25T10:15:30"
  }
]

```

---

## 🧠 Architecture Decision Records (ADR)

Significant architectural choices are documented in the `adr/` directory to provide context on *why* specific technical paths were chosen.


---

## 📄 License

This project was developed for educational and technical assessment purposes. Feel free to explore, fork, and use it as a reference for building event-driven systems in Java.

