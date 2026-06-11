# Development Progress Log

> Building a distributed payment processing platform in Java Spring Boot.
> This log tracks what was built and key decisions made

---

## Phase 1 — Foundation ✅
**Completed: 11 June 2026**

### What was built
- API Gateway with Spring Cloud Gateway routing all external traffic
- Merchant API key authentication — gateway validates key against payment
  service database, stamps X-Merchant-ID on forwarded requests
- POST /api/v1/payments endpoint returning 202 Accepted
- GET /api/v1/payments/{id} for status polling
- Flyway database migrations — payments, outbox_events, merchants tables
- Idempotency key check — duplicate requests return original response

### Key decisions
- Merchant data lives in payment service, not gateway — gateway stays
  thin and stateless, no database of its own
- Gateway calls /internal/merchants/validate to resolve API key to
  merchantId
- /internal endpoints restricted by IP — only reachable from localhost
  in dev, unreachable externally in production
- 202 Accepted not 200 OK — payment is queued not completed instantly


## Phase 2 — Core Saga 
**In progress — Started June 2026**

### Part 1 — Outbox Pattern 

#### What was built
- OutboxPublisher — @Scheduled poller running every 500ms
- Outbox event written atomically with payment record in same
  @Transactional block — either both commit or both rollback
- PaymentInitiated payload built from Payment entity and serialised
  to JSON using ObjectMapper
- Poller publishes PENDING outbox events to correct Kafka topic
  based on event type
- On successful Kafka publish — status flips PENDING → PUBLISHED
  with published_at timestamp

#### Key decisions
- Payload is a separate record not the full Payment entity — consuming
  services only get what they need, no internal fields exposed
- paymentId used as Kafka message key — guarantees all events for same
  payment land on same partition in order
- Kafka auto-creates topics 
- .get(5, TimeUnit.SECONDS) blocks until Kafka confirms receipt —
  event stays PENDING until we know Kafka got it


### Part 2 — Saga Happy Path 
*Next — wire up Fraud Service Kafka consumer*

### Part 3 — Saga Compensation 
*Not started*

---

## Phase 3 — Resilience + Wiring ⬜
*Not started*

---

## Phase 4 — Observability + Testing ⬜
*Not started*

---

## Phase 5 — Deploy + Apply 
*Not started*