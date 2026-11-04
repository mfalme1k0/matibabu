ALTER TABLE patients ADD COLUMN national_id VARCHAR(50);
ALTER TABLE patients ADD COLUMN medical_record_number VARCHAR(100);
ALTER TABLE patients ADD COLUMN email VARCHAR(255);
ALTER TABLE patients ADD COLUMN residence VARCHAR(255);
ALTER TABLE patients ADD COLUMN emergency_contact_name VARCHAR(255);
ALTER TABLE patients ADD COLUMN emergency_contact_phone VARCHAR(50);
ALTER TABLE patients ADD COLUMN emergency_contact_relation VARCHAR(100);
ALTER TABLE patients ADD COLUMN blood_group VARCHAR(10);
ALTER TABLE patients ADD COLUMN insurance_provider VARCHAR(255);
ALTER TABLE patients ADD COLUMN insurance_policy_number VARCHAR(100);
ALTER TABLE patients ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE patients ADD COLUMN updated_at TIMESTAMP;

CREATE UNIQUE INDEX uk_patients_medical_record_number
    ON patients (medical_record_number);