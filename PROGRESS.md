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


## Phase 2 — Core Saga 🔄
*In progress*

---

## Phase 3 — Resilience + Wiring ⬜
*Not started*

---

## Phase 4 — Observability + Testing ⬜
*Not started*

---

## Phase 5 — Deploy + Apply 
*Not started*