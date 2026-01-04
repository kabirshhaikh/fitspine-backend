CREATE TABLE ai_daily_insights_possible_causes (
    id BIGINT NOT NULL AUTO_INCREMENT,

    possible_causes TEXT,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    id_ai_daily_insights BIGINT NOT NULL,

    CONSTRAINT pk_ai_daily_insights_possible_causes PRIMARY KEY (id),
    CONSTRAINT fk_ai_daily_insights_possible_causes_ai_daily_insights
        FOREIGN KEY (id_ai_daily_insights)
        REFERENCES ai_daily_insights (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
