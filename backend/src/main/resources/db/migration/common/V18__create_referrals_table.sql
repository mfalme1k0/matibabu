-- A referral to another facility/specialty, created in the context
-- of an encounter. May optionally reference the diagnosis that
-- prompted it.
--
-- receiving_facility is free text for now: Matibabu does not yet
-- have a first-class Facility concept. This column is expected to
-- become a foreign key once one is introduced (see
-- docs/decisions/ADR-06... style note in the Referral ADR); until
-- then it is plain data, not a reference.

CREATE TABLE referrals (
                           id CHAR(36) NOT NULL PRIMARY KEY,
                           encounter_id CHAR(36) NOT NULL,
                           patient_id CHAR(36) NOT NULL,
                           referring_clinician_id CHAR(36) NOT NULL,
                           diagnosis_id CHAR(36),
                           reason VARCHAR(1000) NOT NULL,
                           urgency VARCHAR(20) NOT NULL,
                           receiving_facility VARCHAR(255) NOT NULL,
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
                                   REFERENCES clinicians(id)
);

CREATE INDEX ix_referrals_encounter_id ON referrals (encounter_id);
CREATE INDEX ix_referrals_patient_id ON referrals (patient_id);
CREATE INDEX ix_referrals_status ON referrals (status);
