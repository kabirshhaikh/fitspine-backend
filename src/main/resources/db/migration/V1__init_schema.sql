CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,

    age INT NOT NULL,

    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    password VARCHAR(100) NOT NULL,

    gender ENUM ('FEMALE', 'MALE', 'OTHER') NOT NULL,
    role ENUM ('ADMIN', 'USER') DEFAULT NULL,
    wearable_type ENUM ('FITBIT', 'GARMIN') DEFAULT NULL,

    is_research_opt BIT(1) NOT NULL,
    surgery_history BIT(1) NOT NULL,
    is_wearable_connected BIT(1) DEFAULT NULL,

    profile_picture VARCHAR(255) DEFAULT NULL,
    public_id VARCHAR(255) DEFAULT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) DEFAULT NULL,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_public_id UNIQUE (public_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
