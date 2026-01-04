CREATE TABLE user_wearable_token (
    id BIGINT NOT NULL AUTO_INCREMENT,

    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL,

    expires_at DATETIME(6) DEFAULT NULL,

    provider VARCHAR(255) NOT NULL,
    token_type VARCHAR(255) DEFAULT NULL,

    scope TEXT,

    revoked BIT(1) NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) DEFAULT NULL,

    user_id BIGINT NOT NULL,

    CONSTRAINT pk_user_wearable_token PRIMARY KEY (id),
    CONSTRAINT fk_user_wearable_token_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
