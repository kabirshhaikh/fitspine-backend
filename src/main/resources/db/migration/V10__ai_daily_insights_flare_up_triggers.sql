CREATE TABLE ai_daily_insights_flare_up_triggers (
    id BIGINT NOT NULL AUTO_INCREMENT,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    id_ai_daily_insights BIGINT NOT NULL,

    impact TEXT,
    metric VARCHAR(255) DEFAULT NULL,
    value VARCHAR(255) DEFAULT NULL,

    CONSTRAINT pk_ai_daily_insights_flare_up_triggers PRIMARY KEY (id),
    CONSTRAINT fk_ai_daily_insights_flare_up_triggers_ai_daily_insights
        FOREIGN KEY (id_ai_daily_insights)
        REFERENCES ai_daily_insights (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
