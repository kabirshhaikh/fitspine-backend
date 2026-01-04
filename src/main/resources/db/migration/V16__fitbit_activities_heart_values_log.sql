CREATE TABLE fitbit_activities_heart_values_log (
    id BIGINT NOT NULL AUTO_INCREMENT,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    resting_heart_rate INT DEFAULT NULL,

    id_fitbit_activities_heart_log BIGINT NOT NULL,

    CONSTRAINT pk_fitbit_activities_heart_values_log PRIMARY KEY (id),
    CONSTRAINT fk_fitbit_activities_heart_values_log_heart_log
        FOREIGN KEY (id_fitbit_activities_heart_log)
        REFERENCES fitbit_activities_heart_log (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
