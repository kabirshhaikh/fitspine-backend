CREATE TABLE manual_daily_pain_locations_log (
    id BIGINT NOT NULL AUTO_INCREMENT,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    pain_location ENUM (
        'ARM',
        'BUTTOCK',
        'LEG',
        'LOW_BACK',
        'MID_BACK',
        'NECK',
        'OTHER',
        'SHOULDER'
    ) DEFAULT NULL,

    id_manual_daily_log BIGINT NOT NULL,

    CONSTRAINT pk_manual_daily_pain_locations_log PRIMARY KEY (id),
    CONSTRAINT fk_manual_daily_pain_locations_log_manual_daily_log
        FOREIGN KEY (id_manual_daily_log)
        REFERENCES manual_daily_log (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
