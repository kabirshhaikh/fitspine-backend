CREATE TABLE fitbit_activity_summaries_distances_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    activity VARCHAR(255) DEFAULT NULL,
    distance DOUBLE DEFAULT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    id_fitbit_activity_summaries_log BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_fitbit_activity_summary_distance
        FOREIGN KEY (id_fitbit_activity_summaries_log)
        REFERENCES fitbit_activity_summaries_log (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
