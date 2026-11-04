-- Restructure treatments from a free-text description into
-- a structured prescription.
--
-- SQLite does not support ALTER COLUMN or ADD CONSTRAINT FOREIGN KEY,
-- so the table is recreated with the new schema.
--
-- Existing treatment rows are preserved:
--   description -> notes
--   new prescription fields -> NULL for historical records

CREATE TABLE medical_record_treatments_new (
                                               id CHAR(36) NOT NULL PRIMARY KEY,
                                               medical_record_id CHAR(36) NOT NULL,
                                               notes VARCHAR(1000),
                                               prescribed_at TIMESTAMP NOT NULL,

                                               medicine_id CHAR(36),
                                               prescribed_by_clinician_id CHAR(36),
                                               dose VARCHAR(50),
                                               dose_unit VARCHAR(20),
                                               route VARCHAR(50),
                                               frequency VARCHAR(50),
                                               duration_days INT,

                                               CONSTRAINT fk_treatment_medical_record
                                                   FOREIGN KEY (medical_record_id)
                                                       REFERENCES medicalrecords(id)
                                                       ON DELETE CASCADE,

                                               CONSTRAINT fk_treatment_medicine
                                                   FOREIGN KEY (medicine_id)
                                                       REFERENCES medicines(id),

                                               CONSTRAINT fk_treatment_clinician
                                                   FOREIGN KEY (prescribed_by_clinician_id)
                                                       REFERENCES clinicians(id)
);

-- Preserve all existing treatment data.
-- The old description becomes notes.
INSERT INTO medical_record_treatments_new (
    id,
    medical_record_id,
    notes,
    prescribed_at
)
SELECT
    id,
    medical_record_id,
    description,
    prescribed_at
FROM medical_record_treatments;

-- Replace the old table.
DROP TABLE medical_record_treatments;

ALTER TABLE medical_record_treatments_new
    RENAME TO medical_record_treatments;

-- Recreate indexes from the original table and add indexes
-- for the new foreign-key columns.
CREATE INDEX ix_treatments_medical_record_id
    ON medical_record_treatments (medical_record_id);

CREATE INDEX ix_treatments_medicine_id
    ON medical_record_treatments (medicine_id);

CREATE INDEX ix_treatments_prescribed_by
    ON medical_record_treatments (prescribed_by_clinician_id);