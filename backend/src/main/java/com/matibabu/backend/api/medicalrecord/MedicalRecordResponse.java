package com.matibabu.backend.api.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.Allergy;
import com.matibabu.backend.domain.medicalrecord.ClinicalObservation;
import com.matibabu.backend.domain.medicalrecord.Diagnosis;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.Treatment;
import com.matibabu.backend.domain.medicalrecord.Vital;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MedicalRecordResponse(
        UUID id,
        UUID patientId,
        UUID encounterId,
        Instant createdAt,
        List<Vital> vitals,
        List<ClinicalObservation> observations,
        List<Diagnosis> diagnoses,
        List<Treatment> treatments,
        List<Allergy> allergies
) {

    public static MedicalRecordResponse from(
            MedicalRecord medicalRecord
    ) {
        return new MedicalRecordResponse(
                medicalRecord.getId(),
                medicalRecord.getPatientId(),
                medicalRecord.getEncounterId(),
                medicalRecord.getCreatedAt(),
                medicalRecord.getVitals(),
                medicalRecord.getObservations(),
                medicalRecord.getDiagnoses(),
                medicalRecord.getTreatments(),
                medicalRecord.getAllergies()
        );
    }
}
