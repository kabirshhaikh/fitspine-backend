CREATE TABLE fitbit_sleep_short_data_log (
    id BIGINT NOT NULL AUTO_INCREMENT,

    date_time DATETIME(6) DEFAULT NULL,
    level VARCHAR(255) DEFAULT NULL,
    seconds INT DEFAULT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    id_fitbit_sleep_log BIGINT NOT NULL,

    CONSTRAINT pk_fitbit_sleep_short_data_log PRIMARY KEY (id),
    CONSTRAINT fk_fitbit_sleep_short_data_log_fitbit_sleep_log
        FOREIGN KEY (id_fitbit_sleep_log)
        REFERENCES fitbit_sleep_log (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
