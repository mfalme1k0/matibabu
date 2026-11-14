CREATE TABLE medical_record_allergies (
    id CHAR(36) NOT NULL PRIMARY KEY,
    medical_record_id CHAR(36) NOT NULL,
    allergen VARCHAR(255) NOT NULL,
    reaction VARCHAR(1000) NOT NULL,
    recorded_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_allergy_medical_record
        FOREIGN KEY (medical_record_id)
            REFERENCES medicalrecords(id)
            ON DELETE CASCADE
);

CREATE INDEX ix_allergies_medical_record_id
    ON medical_record_allergies (medical_record_id);