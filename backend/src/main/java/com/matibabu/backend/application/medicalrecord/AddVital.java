package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import com.matibabu.backend.domain.medicalrecord.Vital;
import com.matibabu.backend.domain.medicalrecord.VitalType;
import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AddVital {

    private final MedicalRecordRepository medicalRecordRepository;
    private final SyncOutboxRecorder syncOutboxRecorder;

    public AddVital(MedicalRecordRepository medicalRecordRepository, SyncOutboxRecorder syncOutboxRecorder) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.syncOutboxRecorder = syncOutboxRecorder;
    }

    @Transactional
    public MedicalRecord execute(
        UUID medicalRecordId,
        VitalType type,
        String value,
        String unit
    ) {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Medical record not found: " + medicalRecordId
            ));

        Vital vital = new Vital(
            medicalRecordId,
            type,
            value,
            unit
        );

        medicalRecord.addVital(vital);

        MedicalRecord saved = medicalRecordRepository.save(medicalRecord);

        Map<String, Object> payload = new HashMap<>();
        payload.put("vitalId", vital.getId().toString());
        payload.put("type", type != null ? type.name() : null);
        payload.put("value", value);
        payload.put("unit", unit);

        syncOutboxRecorder.record(AggregateType.MEDICAL_RECORD, medicalRecordId, "VitalAdded", payload);

        return saved;
    }
}
