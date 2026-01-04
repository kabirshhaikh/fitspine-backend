CREATE TABLE user_disc_issues (
    id BIGINT NOT NULL AUTO_INCREMENT,

    created_at DATETIME(6) NOT NULL,

    disc_level ENUM (
        'C1_C2','C2_C3','C3_C4','C4_C5','C5_C6','C6_C7','C7_T1',
        'L1_L2','L2_L3','L3_L4','L4_L5','L5_S1',
        'OTHER',
        'T10_T11','T11_T12','T12_L1','T1_T2','T2_T3','T3_T4','T4_T5',
        'T5_T6','T6_T7','T7_T8','T8_T9','T9_T10'
    ) NOT NULL,

    user_id BIGINT NOT NULL,

    CONSTRAINT pk_user_disc_issues PRIMARY KEY (id),
    CONSTRAINT fk_user_disc_issues_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
