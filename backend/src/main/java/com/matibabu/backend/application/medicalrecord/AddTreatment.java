package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import com.matibabu.backend.domain.medicalrecord.Treatment;
import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AddTreatment {

    private final MedicalRecordRepository medicalRecordRepository;
    private final SyncOutboxRecorder syncOutboxRecorder;

    public AddTreatment(MedicalRecordRepository medicalRecordRepository, SyncOutboxRecorder syncOutboxRecorder) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.syncOutboxRecorder = syncOutboxRecorder;
    }

    @Transactional
    public MedicalRecord execute(
            UUID medicalRecordId,
            UUID medicineId,
            UUID prescribedByClinicianId,
            String dose,
            String doseUnit,
            String route,
            String frequency,
            Integer durationDays,
            String notes
    ) {
        MedicalRecord medicalRecord =
                medicalRecordRepository.findById(medicalRecordId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Medical record not found: " + medicalRecordId
                        ));

        Treatment treatment = new Treatment(
                medicalRecordId,
                medicineId,
                prescribedByClinicianId,
                dose,
                doseUnit,
                route,
                frequency,
                durationDays,
                notes
        );

        medicalRecord.addTreatment(treatment);

        MedicalRecord saved = medicalRecordRepository.save(medicalRecord);

        Map<String, Object> payload = new HashMap<>();
        payload.put("treatmentId", treatment.getId().toString());
        payload.put("medicineId", medicineId.toString());
        payload.put("prescribedByClinicianId", prescribedByClinicianId.toString());
        payload.put("dose", dose);
        payload.put("doseUnit", doseUnit);
        payload.put("route", route);
        payload.put("frequency", frequency);
        payload.put("durationDays", durationDays);
        payload.put("notes", notes);

        syncOutboxRecorder.record(AggregateType.MEDICAL_RECORD, medicalRecordId, "TreatmentAdded", payload);

        return saved;
    }
}