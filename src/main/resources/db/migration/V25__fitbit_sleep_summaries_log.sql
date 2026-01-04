CREATE TABLE fitbit_sleep_summaries_log (
    id BIGINT NOT NULL AUTO_INCREMENT,

    log_date DATE NOT NULL,

    provider VARCHAR(255) NOT NULL,

    total_minutes_asleep INT DEFAULT NULL,
    total_sleep_records INT DEFAULT NULL,
    total_time_in_bed INT DEFAULT NULL,

    raw_json LONGTEXT,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    user_id BIGINT NOT NULL,

    CONSTRAINT pk_fitbit_sleep_summaries_log PRIMARY KEY (id),
    CONSTRAINT uk_fitbit_sleep_summaries_log_user_log_date
        UNIQUE (user_id, log_date),
    CONSTRAINT fk_fitbit_sleep_summaries_log_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
