ALTER TABLE payments
    ADD COLUMN merchant_id VARCHAR(255) NOT NULL DEFAULT 'unknown';