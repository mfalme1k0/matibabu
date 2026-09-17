package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.ClinicalObservation;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AddClinicalObservation {

    private final MedicalRecordRepository medicalRecordRepository;
    private final SyncOutboxRecorder syncOutboxRecorder;

    public AddClinicalObservation(MedicalRecordRepository medicalRecordRepository, SyncOutboxRecorder syncOutboxRecorder) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.syncOutboxRecorder = syncOutboxRecorder;
    }

    @Transactional
    public MedicalRecord execute(
        UUID medicalRecordId,
        String description
    ) {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Medical record not found: " + medicalRecordId
            ));

        ClinicalObservation observation =
            new ClinicalObservation(medicalRecordId, description);

        medicalRecord.addObservation(observation);

        MedicalRecord saved = medicalRecordRepository.save(medicalRecord);

        Map<String, Object> payload = new HashMap<>();
        payload.put("observationId", observation.getId().toString());
        payload.put("description", description);

        syncOutboxRecorder.record(AggregateType.MEDICAL_RECORD, medicalRecordId, "ClinicalObservationAdded", payload);

        return saved;
    }
}