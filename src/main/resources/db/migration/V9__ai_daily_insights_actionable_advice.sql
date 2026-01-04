CREATE TABLE ai_daily_insights_actionable_advice (
    id BIGINT NOT NULL AUTO_INCREMENT,

    advice TEXT,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    id_ai_daily_insights BIGINT NOT NULL,

    CONSTRAINT pk_ai_daily_insights_actionable_advice PRIMARY KEY (id),
    CONSTRAINT fk_ai_daily_insights_actionable_advice_ai_daily_insights
        FOREIGN KEY (id_ai_daily_insights)
        REFERENCES ai_daily_insights (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
