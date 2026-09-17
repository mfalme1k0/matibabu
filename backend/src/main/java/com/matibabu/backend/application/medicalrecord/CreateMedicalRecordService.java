package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.exception.EncounterNotFoundException;
import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class CreateMedicalRecordService implements CreateMedicalRecordUseCase {

    private final MedicalRecordRepository medicalRecordRepository;
    private final EncounterRepository encounterRepository;
    private final SyncOutboxRecorder syncOutboxRecorder;

    public CreateMedicalRecordService(
            MedicalRecordRepository medicalRecordRepository,
            EncounterRepository encounterRepository,
            SyncOutboxRecorder syncOutboxRecorder
    ) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.encounterRepository = encounterRepository;
        this.syncOutboxRecorder = syncOutboxRecorder;
    }

    @Override
    @Transactional
    public MedicalRecord create(UUID encounterId) {

        /*
         * Find the encounter first.
         *
         * A medical record cannot exist for an encounter
         * that does not exist.
         */
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(
                        () -> new EncounterNotFoundException(encounterId)
                );

        /*
         * The encounter already knows which patient it belongs to.
         * We use that patient ID when creating the medical record.
         */
        UUID patientId = encounter.getPatientId();

        /*
         * Create the medical record and associate it with
         * both the patient and the encounter.
         */
        MedicalRecord medicalRecord =
                new MedicalRecord(patientId, encounterId);

        // Persist the new medical record.
        MedicalRecord saved = medicalRecordRepository.save(medicalRecord);

        syncOutboxRecorder.record(
                AggregateType.MEDICAL_RECORD,
                saved.getId(),
                "MedicalRecordCreated",
                Map.of(
                        "encounterId", encounterId.toString(),
                        "patientId", patientId.toString()
                )
        );

        return saved;
    }
}