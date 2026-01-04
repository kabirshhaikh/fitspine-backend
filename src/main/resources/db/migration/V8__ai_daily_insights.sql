CREATE TABLE ai_daily_insights (
    id BIGINT NOT NULL AUTO_INCREMENT,

    completion_tokens INT DEFAULT NULL,
    prompt_tokens INT DEFAULT NULL,
    total_tokens INT DEFAULT NULL,

    disc_protection_score INT DEFAULT NULL,
    disc_score_explanation TEXT,

    todays_insights TEXT,
    recovery_insights TEXT,

    model_used VARCHAR(255) DEFAULT NULL,

    provider ENUM ('FITBIT','GARMIN') DEFAULT NULL,

    log_date DATE NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    user_id BIGINT NOT NULL,

    CONSTRAINT pk_ai_daily_insights PRIMARY KEY (id),
    CONSTRAINT fk_ai_daily_insights_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
