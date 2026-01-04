CREATE TABLE manual_daily_log (
    id BIGINT NOT NULL AUTO_INCREMENT,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    log_date DATE NOT NULL,

    pain_level ENUM ('MILD','MODERATE','NONE','SEVERE') DEFAULT NULL,

    flare_up_today BIT(1) DEFAULT NULL,
    numbness_tingling BIT(1) DEFAULT NULL,
    lifting_or_strain BIT(1) DEFAULT NULL,
    stretching_done BIT(1) DEFAULT NULL,

    sitting_time ENUM (
        'FOUR_TO_SIX_HOURS',
        'GREATER_THAN_EIGHT_HOURS',
        'LESS_THAN_TWO_HOURS',
        'SIX_TO_EIGHT_HOURS',
        'TWO_TO_FOUR_HOURS'
    ) DEFAULT NULL,

    standing_time ENUM (
        'FOUR_TO_SIX_HOURS',
        'GREATER_THAN_EIGHT_HOURS',
        'LESS_THAN_TWO_HOURS',
        'SIX_TO_EIGHT_HOURS',
        'TWO_TO_FOUR_HOURS'
    ) DEFAULT NULL,

    morning_stiffness ENUM ('MILD','MODERATE','NONE','SEVERE') DEFAULT NULL,

    stress_level ENUM ('HIGH','LOW','MODERATE','VERY_HIGH','VERY_LOW') DEFAULT NULL,

    notes TEXT,

    sleep_duration ENUM (
        'H5_TO_6',
        'H6_TO_7',
        'H7_TO_8',
        'LESS_THAN_5H',
        'MORE_THAN_8H'
    ) DEFAULT NULL,

    night_wake_ups ENUM ('NONE','ONE','THREE_OR_MORE','TWO') DEFAULT NULL,

    resting_heart_rate INT DEFAULT NULL,

    user_id BIGINT NOT NULL,

    CONSTRAINT pk_manual_daily_log PRIMARY KEY (id),
    CONSTRAINT fk_manual_daily_log_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
