CREATE TABLE fitbit_sleep_summary_stages_log (
    id BIGINT NOT NULL AUTO_INCREMENT,

    deep INT DEFAULT NULL,
    light INT DEFAULT NULL,
    rem INT DEFAULT NULL,
    wake INT DEFAULT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    id_fitbit_sleep_summaries_log BIGINT NOT NULL,

    CONSTRAINT pk_fitbit_sleep_summary_stages_log PRIMARY KEY (id),
    CONSTRAINT uk_fitbit_sleep_summary_stages_log_sleep_summaries_log
        UNIQUE (id_fitbit_sleep_summaries_log),
    CONSTRAINT fk_fitbit_sleep_summary_stages_log_sleep_summaries_log
        FOREIGN KEY (id_fitbit_sleep_summaries_log)
        REFERENCES fitbit_sleep_summaries_log (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
