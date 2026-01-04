CREATE TABLE fitbit_activities_heart_log (
    id BIGINT NOT NULL AUTO_INCREMENT,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    date_time DATE DEFAULT NULL,
    log_date DATE NOT NULL,

    provider VARCHAR(255) NOT NULL,

    raw_json LONGTEXT,

    user_id BIGINT NOT NULL,

    CONSTRAINT pk_fitbit_activities_heart_log PRIMARY KEY (id),
    CONSTRAINT uk_fitbit_activities_heart_log_user_log_date
        UNIQUE (user_id, log_date),
    CONSTRAINT fk_fitbit_activities_heart_log_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
