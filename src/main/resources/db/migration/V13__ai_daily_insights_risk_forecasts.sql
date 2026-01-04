CREATE TABLE ai_daily_insights_risk_forecasts (
    id BIGINT NOT NULL AUTO_INCREMENT,

    flareup_risk_score INT DEFAULT NULL,
    pain_risk_score INT DEFAULT NULL,
    risk_bucket VARCHAR(255) DEFAULT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    id_ai_daily_insights BIGINT NOT NULL,

    CONSTRAINT pk_ai_daily_insights_risk_forecasts PRIMARY KEY (id),
    CONSTRAINT uk_ai_daily_insights_risk_forecasts_ai_daily_insights
        UNIQUE (id_ai_daily_insights),
    CONSTRAINT fk_ai_daily_insights_risk_forecasts_ai_daily_insights
        FOREIGN KEY (id_ai_daily_insights)
        REFERENCES ai_daily_insights (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
