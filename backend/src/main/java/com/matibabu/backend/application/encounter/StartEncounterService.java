package com.matibabu.backend.application.encounter;

import com.matibabu.backend.config.NodeIdentity;
import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class StartEncounterService implements StartEncounterUseCase {

    // Repository handles persistence.
    // The Encounter domain object handles the business rules.
    private final EncounterRepository encounterRepository;
    private final NodeIdentity nodeIdentity;
    private final SyncOutboxRecorder syncOutboxRecorder;

    public StartEncounterService(
            EncounterRepository encounterRepository,
            NodeIdentity nodeIdentity,
            SyncOutboxRecorder syncOutboxRecorder
    ) {
        this.encounterRepository = encounterRepository;
        this.nodeIdentity = nodeIdentity;
        this.syncOutboxRecorder = syncOutboxRecorder;
    }

    @Override
    @Transactional
    public Encounter start(
            UUID patientId,
            UUID attendingClinicianId,
            Instant now
    ) {
        Encounter encounter =
                Encounter.start(patientId, attendingClinicianId, nodeIdentity.facilityId(), now);

        Encounter saved = encounterRepository.save(encounter);

        syncOutboxRecorder.record(
                AggregateType.ENCOUNTER,
                saved.getId(),
                "EncounterStarted",
                Map.of(
                        "patientId", patientId.toString(),
                        "attendingClinicianId", attendingClinicianId.toString(),
                        "startedAt", now.toString()
                )
        );

        return saved;
    }
}