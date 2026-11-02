
CREATE DATABASE IF NOT EXISTS idempotency;

USE idempotency;

CREATE TABLE IF NOT EXISTS idempotency_keys (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                idem_key VARCHAR(64) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_body TEXT NOT NULL,
    status_code INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,

    CONSTRAINT uk_idem_key UNIQUE (idem_key),
    INDEX idx_expires (expires_at)
    ) ENGINE=InnoDB;
