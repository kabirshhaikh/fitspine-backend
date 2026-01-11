CREATE TABLE feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,

    feedback_type ENUM (
        'BUG_REPORT',
        'FEATURE_REQUEST',
        'GENERAL_FEEDBACK',
        'OTHER'
    ) NOT NULL,

    subject VARCHAR(255) NOT NULL,

    description TEXT NOT NULL,

    rating INT NULL,

    contact_requested BOOLEAN NOT NULL DEFAULT FALSE,

    created_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_feedback PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
