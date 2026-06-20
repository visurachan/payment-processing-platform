# Distributed Payment Processing Platform

A distributed payment orchestration system built with **Java 21**, **Spring Boot 3**, and **Apache Kafka**. It coordinates cross-bank payments across multiple independent bank instances using a saga choreography pattern with guaranteed exactly-once processing via the outbox pattern.

Built as an extension of the [banking-core-api](https://github.com/visurachan/banking-core-api) ecosystem. Two instances of [banking-core-standalone](https://github.com/visurachan/banking-core-standalone) act as the simulated banks.

> 🚧 **Actively in development.** This project is being built incrementally, phase
> by phase. Follow real-time progress, architectural decisions, and problems
> solved along the way in [PROGRESS.md](./PROGRESS.md).


---

## Services

| Service | Port | Responsibility |
|---|---|---|
| API Gateway | 8080 | JWT validation, rate limiting, request routing |
| Payment Service | 8081 | Payment lifecycle, outbox pattern, saga coordination |
| Account Service | 8082 | Debit/credit calls to bank instances, idempotency |
| Webhook Service | 8083 | Merchant callback notifications on payment outcome |
| Fraud Service | 8084 | Real-time fraud checks via Kafka |
| Audit Service | 8085 | Immutable append-only event log across all topics |
| Bank A | 8090 | Simulated bank — independent ledger and database |
| Bank B | 8091 | Simulated bank — independent ledger and database |

---

## Architecture

```
Client
  │
  ▼
API Gateway :8080  (JWT · Rate Limiter · Routing)
  │
  ▼
Payment Service :8081  (Outbox → Kafka)
  │
  ├──► Fraud Service :8084   (Kafka consumer)
  │
  ├──► Account Service :8082  (Debit Bank A · Credit Bank B)
  │         ├── Bank A :8090
  │         └── Bank B :8091
  │
  ├──► Webhook Service :8083  (Merchant callbacks)
  │
  └──► Audit Service :8085   (All topics → append-only log)
```

---

## Key Patterns

- **Saga choreography** — services react to Kafka events, no central orchestrator
- **Outbox pattern** — payment and Kafka publish written atomically; no dual-write risk
- **Idempotency** — safe retries at both the API and the account coordination layer
- **Circuit breaker** — Resilience4j wraps all HTTP calls to bank instances
- **Distributed tracing** — Zipkin trace spans across all services per payment

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Gateway | Spring Cloud Gateway |
| Messaging | Apache Kafka |
| Databases | PostgreSQL 15 (one per service) |
| Migrations | Flyway |
| Resilience | Resilience4j (circuit breaker, retry, bulkhead) |
| Tracing | Zipkin + Micrometer |
| Containerisation | Docker Compose |
| Build | Maven |

---

## Related

- [banking-core-api](https://github.com/visurachan/banking-core-api) — the original full-featured banking service
- [banking-core-standalone](https://github.com/visurachan/banking-core-standalone) — stripped-down bank instances used by this platform
- [fraud-detection-service](https://github.com/visurachan/fraud-detection-service) — existing fraud service integrated via Kafka
