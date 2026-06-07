CREATE TABLE payments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key  VARCHAR(255) NOT NULL UNIQUE,
    source_bank_id   VARCHAR(50)  NOT NULL,
    source_account_id       VARCHAR(255) NOT NULL,
    destination_bank_id     VARCHAR(50)  NOT NULL,
    destination_account_id  VARCHAR(255) NOT NULL,
    amount           DECIMAL(19, 4) NOT NULL CHECK (amount > 0),
    currency         VARCHAR(3)   NOT NULL,
    status           VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    failure_reason   TEXT,
    merchant_callback_url   VARCHAR(500),
    version          INTEGER      NOT NULL DEFAULT 0,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_status     ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at);
