package com.matibabu.backend.application.encounter;

import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.exception.EncounterNotFoundException;
import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class DischargeEncounterService implements DischargeEncounterUseCase {

    private final EncounterRepository encounterRepository;
    private final SyncOutboxRecorder syncOutboxRecorder;

    public DischargeEncounterService(
            EncounterRepository encounterRepository,
            SyncOutboxRecorder syncOutboxRecorder
    ) {
        this.encounterRepository = encounterRepository;
        this.syncOutboxRecorder = syncOutboxRecorder;
    }

    @Override
    @Transactional
    public void discharge(UUID encounterId, Instant now) {

        Encounter encounter =
                encounterRepository.findById(encounterId)
                        .orElseThrow(
                                () -> new EncounterNotFoundException(encounterId)
                        );

        // The domain controls whether the encounter can be discharged.

        encounter.discharge(now);

        // Persist updated encounter.
        encounterRepository.save(encounter);

        syncOutboxRecorder.record(
                AggregateType.ENCOUNTER,
                encounterId,
                "EncounterDischarged",
                Map.of("endedAt", now.toString())
        );
    }
}