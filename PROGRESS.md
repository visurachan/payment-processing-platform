# Development Progress Log

> Building a distributed payment processing platform in Java Spring Boot.
> This log tracks what was built and key decisions made

---

## Phase 1 — Foundation ✅


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


### Part 2 — Fraud Service ✅

#### What was built
- Fraud service as independent stateless service in payment platform
- Consumes PaymentInitiated events from payments.initiated Kafka topic
- FraudRuleEngine with 3 rules:
  - Self payment — source and destination same account same bank
  - Cross-bank large amount — over £5000 between different banks
  - Invalid amount — null or zero amount
- Publishes FraudChecked event to payments.fraud.checked topic
- No database — purely event driven, consume and publish

#### Key decisions (Updated)
- Kept fraud service lightweight —  three simple rules
  that only the platform can detect, not the individual bank
- Stateless design — no persistence needed, rules run in memory
- Separate from banking core fraud service — different concerns,
  different layer, different rules
- Added 4 payment processor endpoints to banking-core-standalone —
debit, credit, debit/reverse, exists — see
    [banking-core-standalone](https://github.com/visurachan/banking-core-standalone#payment-processor-api)

- Fat events — FraudCheckedEvent carries full payment context so Account
Service is completely decoupled from Payment Service. No REST calls
between platform services — every service gets what it needs from Kafka
alone
- merchantId and merchantCallbackUrl deliberately excluded from the event
  chain — Payment Service reads these from its own payments table when
  publishing PaymentCompleted. Each service reads its own data rather than
  passing fields through events unnecessarily
- Rule engine separation — FraudRuleEngine only sets fraud decision fields
  (result, reason). FraudEventConsumer handles payment context enrichment
  separately. Single responsibility — rule engine decides fraud, consumer
  handles event construction


### Part 3 — Accounts Service + Saga Happy Path
*Next — Account service consumes FraudChecked, call banking core, publish AccountDebited*

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