
CREATE TABLE referrals_new (
                               id CHAR(36) NOT NULL PRIMARY KEY,
                               encounter_id CHAR(36) NOT NULL,
                               patient_id CHAR(36) NOT NULL,
                               referring_clinician_id CHAR(36) NOT NULL,
                               diagnosis_id CHAR(36),
                               reason VARCHAR(1000) NOT NULL,
                               urgency VARCHAR(20) NOT NULL,
                               receiving_facility_id CHAR(36) NOT NULL,
                               department VARCHAR(255),
                               status VARCHAR(20) NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               resolved_by CHAR(36),
                               resolved_at TIMESTAMP,

                               CONSTRAINT fk_referral_encounter
                                   FOREIGN KEY (encounter_id)
                                       REFERENCES encounters(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_referral_patient
                                   FOREIGN KEY (patient_id)
                                       REFERENCES patients(id),

                               CONSTRAINT fk_referral_diagnosis
                                   FOREIGN KEY (diagnosis_id)
                                       REFERENCES medical_record_diagnoses(id),

                               CONSTRAINT fk_referral_referring_clinician
                                   FOREIGN KEY (referring_clinician_id)
                                       REFERENCES clinicians(id),

                               CONSTRAINT fk_referral_resolved_by
                                   FOREIGN KEY (resolved_by)
                                       REFERENCES clinicians(id),

                               CONSTRAINT fk_referral_receiving_facility
                                   FOREIGN KEY (receiving_facility_id)
                                       REFERENCES facilities(id)
);

INSERT INTO referrals_new (
    id, encounter_id, patient_id, referring_clinician_id, diagnosis_id,
    reason, urgency, department, status, created_at, resolved_by, resolved_at
)
SELECT
    id, encounter_id, patient_id, referring_clinician_id, diagnosis_id,
    reason, urgency, department, status, created_at, resolved_by, resolved_at
FROM referrals;

DROP TABLE referrals;

ALTER TABLE referrals_new
    RENAME TO referrals;

CREATE INDEX ix_referrals_encounter_id ON referrals (encounter_id);
CREATE INDEX ix_referrals_patient_id ON referrals (patient_id);
CREATE INDEX ix_referrals_status ON referrals (status);
CREATE INDEX ix_referrals_receiving_facility_id ON referrals (receiving_facility_id);