CREATE TABLE user_injuries (
    id BIGINT NOT NULL AUTO_INCREMENT,

    created_at DATETIME(6) NOT NULL,

    injury_type ENUM (
        'ANNULAR_TEAR',
        'BULGING_DISC',
        'CERVICAL_RADICULOPATHY',
        'DEGENERATIVE_DISC_DISEASE',
        'DISC_EXTRUSION',
        'DISC_SEQUESTRATION',
        'FACET_JOINT_SYNDROME',
        'HERNIATED_DISC',
        'LUMBAR_RADICULOPATHY',
        'NON_SPECIFIC_LOWER_BACK_PAIN',
        'NON_SPECIFIC_NECK_PAIN',
        'OTHER',
        'POST_SURGICAL_PAIN',
        'SCIATICA',
        'SPINAL_FRACTURE',
        'SPINAL_INFECTION',
        'SPINAL_STENOSIS',
        'SPINAL_TUMOR',
        'SPONDYLOLISTHESIS',
        'THORACIC_DISC_HERNIATION'
    ) NOT NULL,

    user_id BIGINT NOT NULL,

    CONSTRAINT pk_user_injuries PRIMARY KEY (id),
    CONSTRAINT fk_user_injuries_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
