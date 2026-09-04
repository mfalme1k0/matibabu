CREATE TABLE medical_record_vitals (
                                       id CHAR(36) NOT NULL PRIMARY KEY,
                                       medical_record_id CHAR(36) NOT NULL,
                                       type VARCHAR(50) NOT NULL,
                                       value VARCHAR(255) NOT NULL,
                                       unit VARCHAR(50) NOT NULL,
                                       recorded_at TIMESTAMP NOT NULL,

                                       CONSTRAINT fk_vital_medical_record
                                           FOREIGN KEY (medical_record_id)
                                               REFERENCES medicalrecords(id)
                                               ON DELETE CASCADE
);

CREATE INDEX ix_vitals_medical_record_id
    ON medical_record_vitals (medical_record_id);


-- CLINICAL OBSERVATIONS
CREATE TABLE medical_record_observations (
                                             id CHAR(36) NOT NULL PRIMARY KEY,
                                             medical_record_id CHAR(36) NOT NULL,
                                             description VARCHAR(1000) NOT NULL,
                                             recorded_at TIMESTAMP NOT NULL,

                                             CONSTRAINT fk_observation_medical_record
                                                 FOREIGN KEY (medical_record_id)
                                                     REFERENCES medicalrecords(id)
                                                     ON DELETE CASCADE
);

CREATE INDEX ix_observations_medical_record_id
    ON medical_record_observations (medical_record_id);


-- DIAGNOSES
CREATE TABLE medical_record_diagnoses (
                                          id CHAR(36) NOT NULL PRIMARY KEY,
                                          medical_record_id CHAR(36) NOT NULL,
                                          description VARCHAR(1000) NOT NULL,
                                          type VARCHAR(50) NOT NULL,
                                          recorded_at TIMESTAMP NOT NULL,

                                          CONSTRAINT fk_diagnosis_medical_record
                                              FOREIGN KEY (medical_record_id)
                                                  REFERENCES medicalrecords(id)
                                                  ON DELETE CASCADE
);

CREATE INDEX ix_diagnoses_medical_record_id
    ON medical_record_diagnoses (medical_record_id);


-- TREATMENTS
CREATE TABLE medical_record_treatments (
                                           id CHAR(36) NOT NULL PRIMARY KEY,
                                           medical_record_id CHAR(36) NOT NULL,
                                           description VARCHAR(1000) NOT NULL,
                                           prescribed_at TIMESTAMP NOT NULL,

                                           CONSTRAINT fk_treatment_medical_record
                                               FOREIGN KEY (medical_record_id)
                                                   REFERENCES medicalrecords(id)
                                                   ON DELETE CASCADE
);

CREATE INDEX ix_treatments_medical_record_id
    ON medical_record_treatments (medical_record_id);