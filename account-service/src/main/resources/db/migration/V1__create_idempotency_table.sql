CREATE TABLE idempotency_keys (
    idempotency_key  VARCHAR(255) PRIMARY KEY,
    payment_id       UUID         NOT NULL,
    operation        VARCHAR(50)  NOT NULL,
    result           JSONB        NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    expires_at       TIMESTAMP    NOT NULL
);

CREATE INDEX idx_idempotency_expires ON idempotency_keys(expires_at);
