CREATE TABLE fitbit_sleep_log (
    id BIGINT NOT NULL AUTO_INCREMENT,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    log_date DATE NOT NULL,
    date_of_sleep DATE DEFAULT NULL,

    efficiency INT DEFAULT NULL,

    start_time DATETIME(6) DEFAULT NULL,
    end_time DATETIME(6) DEFAULT NULL,

    info_code INT DEFAULT NULL,

    is_main_sleep BIT(1) DEFAULT NULL,

    log_id BIGINT DEFAULT NULL,

    minutes_after_wakeup INT DEFAULT NULL,
    minutes_asleep INT DEFAULT NULL,
    minutes_awake INT DEFAULT NULL,
    minutes_to_fall_asleep INT DEFAULT NULL,

    log_type VARCHAR(255) DEFAULT NULL,

    time_in_bed INT DEFAULT NULL,

    type VARCHAR(255) DEFAULT NULL,

    provider VARCHAR(255) NOT NULL,

    user_id BIGINT NOT NULL,

    CONSTRAINT pk_fitbit_sleep_log PRIMARY KEY (id),
    CONSTRAINT fk_fitbit_sleep_log_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
