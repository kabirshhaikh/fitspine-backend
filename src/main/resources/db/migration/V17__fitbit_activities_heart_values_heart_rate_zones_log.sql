CREATE TABLE fitbit_activities_heart_values_heart_rate_zones_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    calories_out DOUBLE DEFAULT NULL,
    max INT DEFAULT NULL,
    min INT DEFAULT NULL,
    minutes INT DEFAULT NULL,
    name VARCHAR(255) DEFAULT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    id_fitbit_activities_heart_values_log BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_fitbit_hr_zones_values
        FOREIGN KEY (id_fitbit_activities_heart_values_log)
        REFERENCES fitbit_activities_heart_values_log (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
