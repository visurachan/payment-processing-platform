CREATE TABLE merchants (
                           id         VARCHAR(255) PRIMARY KEY,
                           name       VARCHAR(255) NOT NULL,
                           api_key    VARCHAR(255) NOT NULL UNIQUE,
                           active     BOOLEAN      NOT NULL DEFAULT true,
                           created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

INSERT INTO merchants (id, name, api_key) VALUES
                                              ('merchant-001', 'Test Store A', 'sk_test_merchant_001'),
                                              ('merchant-002', 'Test Store B', 'sk_test_merchant_002');