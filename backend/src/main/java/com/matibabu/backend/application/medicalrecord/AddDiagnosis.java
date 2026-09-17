package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.Diagnosis;
import com.matibabu.backend.domain.medicalrecord.DiagnosisType;
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
public class AddDiagnosis {

    private final MedicalRecordRepository medicalRecordRepository;
    private final SyncOutboxRecorder syncOutboxRecorder;

    public AddDiagnosis(MedicalRecordRepository medicalRecordRepository, SyncOutboxRecorder syncOutboxRecorder) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.syncOutboxRecorder = syncOutboxRecorder;
    }

    @Transactional
    public MedicalRecord execute(
            UUID medicalRecordId,
            String description,
            DiagnosisType type
    ) {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Medical record not found: " + medicalRecordId
                ));

        Diagnosis diagnosis = new Diagnosis(
                medicalRecordId,
                description,
                type
        );

        medicalRecord.addDiagnosis(diagnosis);

        MedicalRecord saved = medicalRecordRepository.save(medicalRecord);

        Map<String, Object> payload = new HashMap<>();
        payload.put("diagnosisId", diagnosis.getId().toString());
        payload.put("description", description);
        payload.put("type", type != null ? type.name() : null);

        syncOutboxRecorder.record(AggregateType.MEDICAL_RECORD, medicalRecordId, "DiagnosisAdded", payload);

        return saved;
    }
}
