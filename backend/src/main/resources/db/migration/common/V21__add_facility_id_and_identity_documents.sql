

ALTER TABLE encounters ADD COLUMN facility_id CHAR(36) REFERENCES facilities(id);

CREATE INDEX ix_encounters_facility_id ON encounters (facility_id);


ALTER TABLE patients ADD COLUMN birth_certificate_number VARCHAR(50);

CREATE UNIQUE INDEX ux_patients_national_id ON patients (national_id) WHERE national_id IS NOT NULL;
CREATE UNIQUE INDEX ux_patients_birth_certificate_number ON patients (birth_certificate_number) WHERE birth_certificate_number IS NOT NULL;
