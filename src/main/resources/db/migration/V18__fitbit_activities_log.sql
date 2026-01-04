CREATE TABLE fitbit_activities_log (
    id BIGINT NOT NULL AUTO_INCREMENT,

    activity_id INT DEFAULT NULL,
    activity_parent_id INT DEFAULT NULL,
    activity_parent_name VARCHAR(255) DEFAULT NULL,

    calories INT DEFAULT NULL,
    distance DOUBLE DEFAULT NULL,
    duration BIGINT DEFAULT NULL,
    steps INT DEFAULT NULL,

    description VARCHAR(255) DEFAULT NULL,
    name VARCHAR(255) DEFAULT NULL,

    is_favourite BIT(1) DEFAULT NULL,
    has_active_zone_minutes BIT(1) DEFAULT NULL,
    has_start_time BIT(1) DEFAULT NULL,

    last_modified DATETIME(6) DEFAULT NULL,

    log_date DATE NOT NULL,
    log_id BIGINT DEFAULT NULL,

    start_date DATE DEFAULT NULL,
    start_time TIME(6) DEFAULT NULL,

    provider VARCHAR(255) NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    user_id BIGINT NOT NULL,

    CONSTRAINT pk_fitbit_activities_log PRIMARY KEY (id),
    CONSTRAINT fk_fitbit_activities_log_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
