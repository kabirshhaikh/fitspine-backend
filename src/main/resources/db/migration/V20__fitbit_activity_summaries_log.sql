CREATE TABLE fitbit_activity_summaries_log (
    id BIGINT NOT NULL AUTO_INCREMENT,

    active_score INT DEFAULT NULL,
    activity_calories INT DEFAULT NULL,
    calories_bmr INT DEFAULT NULL,
    calories_out INT DEFAULT NULL,

    sedentary_minutes INT DEFAULT NULL,
    lightly_active_minutes INT DEFAULT NULL,
    fairly_active_minutes INT DEFAULT NULL,
    very_active_minutes INT DEFAULT NULL,

    steps INT DEFAULT NULL,
    marginal_calories INT DEFAULT NULL,

    raw_json LONGTEXT,

    log_date DATE NOT NULL,

    provider VARCHAR(255) NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    user_id BIGINT NOT NULL,

    CONSTRAINT pk_fitbit_activity_summaries_log PRIMARY KEY (id),
    CONSTRAINT uk_fitbit_activity_summaries_log_user_log_date
        UNIQUE (user_id, log_date),
    CONSTRAINT fk_fitbit_activity_summaries_log_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
