package com.matibabu.backend.application.referral;

import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.domain.facility.FacilityRepository;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import com.matibabu.backend.domain.referral.Referral;
import com.matibabu.backend.domain.referral.ReferralRepository;
import com.matibabu.backend.domain.referral.ReferralUrgency;
import com.matibabu.backend.exception.EncounterNotFoundException;
import com.matibabu.backend.exception.FacilityNotFoundException;
import com.matibabu.backend.exception.InvalidDiagnosisReferenceException;
import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CreateReferralService implements CreateReferralUseCase {

    private final ReferralRepository referralRepository;
    private final EncounterRepository encounterRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final FacilityRepository facilityRepository;
    private final SyncOutboxRecorder syncOutboxRecorder;

    public CreateReferralService(
            ReferralRepository referralRepository,
            EncounterRepository encounterRepository,
            MedicalRecordRepository medicalRecordRepository,
            FacilityRepository facilityRepository,
            SyncOutboxRecorder syncOutboxRecorder
    ) {
        this.referralRepository = referralRepository;
        this.encounterRepository = encounterRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.facilityRepository = facilityRepository;
        this.syncOutboxRecorder = syncOutboxRecorder;
    }

    @Override
    @Transactional
    public Referral create(
            UUID encounterId,
            UUID referringClinicianId,
            UUID diagnosisId,
            String reason,
            ReferralUrgency urgency,
            UUID receivingFacilityId,
            String department
    ) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new EncounterNotFoundException(encounterId));

        facilityRepository.findById(receivingFacilityId)
                .orElseThrow(() -> new FacilityNotFoundException(receivingFacilityId));

        if (diagnosisId != null) {
            MedicalRecord medicalRecord = medicalRecordRepository.findByEncounterId(encounterId)
                    .orElseThrow(() -> new InvalidDiagnosisReferenceException(diagnosisId, encounterId));

            boolean diagnosisBelongsToEncounter = medicalRecord.getDiagnoses().stream()
                    .anyMatch(diagnosis -> diagnosis.getId().equals(diagnosisId));

            if (!diagnosisBelongsToEncounter) {
                throw new InvalidDiagnosisReferenceException(diagnosisId, encounterId);
            }
        }

        Referral referral = Referral.create(
                encounterId,
                encounter.getPatientId(),
                referringClinicianId,
                diagnosisId,
                reason,
                urgency,
                receivingFacilityId,
                department,
                Instant.now()
        );

        Referral saved = referralRepository.save(referral);

        Map<String, Object> payload = new HashMap<>();
        payload.put("encounterId", encounterId.toString());
        payload.put("referringClinicianId", referringClinicianId.toString());
        payload.put("diagnosisId", diagnosisId != null ? diagnosisId.toString() : null);
        payload.put("reason", reason);
        payload.put("urgency", urgency != null ? urgency.name() : null);
        payload.put("receivingFacilityId", receivingFacilityId.toString());
        payload.put("department", department);

        syncOutboxRecorder.record(AggregateType.REFERRAL, saved.getId(), "ReferralCreated", payload);

        return saved;
    }
}
