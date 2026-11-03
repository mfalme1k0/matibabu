CREATE TABLE medicalrecords (
                                id CHAR(36) NOT NULL PRIMARY KEY,
                                patient_id CHAR(36) NOT NULL,
                                created_at TIMESTAMP NOT NULL
);