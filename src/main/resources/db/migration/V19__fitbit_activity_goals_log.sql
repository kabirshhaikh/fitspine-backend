CREATE TABLE fitbit_activity_goals_log (
    id BIGINT NOT NULL AUTO_INCREMENT,

    active_minutes INT DEFAULT NULL,
    calories_out INT DEFAULT NULL,
    distance DOUBLE DEFAULT NULL,
    floors INT DEFAULT NULL,
    steps INT DEFAULT NULL,

    log_date DATE DEFAULT NULL,

    provider VARCHAR(255) NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    user_id BIGINT NOT NULL,

    CONSTRAINT pk_fitbit_activity_goals_log PRIMARY KEY (id),
    CONSTRAINT uk_fitbit_activity_goals_log_user_log_date
        UNIQUE (user_id, log_date),
    CONSTRAINT fk_fitbit_activity_goals_log_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
