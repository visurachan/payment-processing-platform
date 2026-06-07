CREATE TABLE payment_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id      UUID         NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         JSONB        NOT NULL,
    sequence_number INTEGER      NOT NULL,
    source_service  VARCHAR(100) NOT NULL,
    occurred_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_events_payment_id ON payment_events(payment_id, sequence_number);
CREATE UNIQUE INDEX idx_events_sequence ON payment_events(payment_id, sequence_number);
