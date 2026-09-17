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
public class CancelEncounterService implements CancelEncounterUseCase {

    private final EncounterRepository encounterRepository;
    private final SyncOutboxRecorder syncOutboxRecorder;

    public CancelEncounterService(EncounterRepository encounterRepository, SyncOutboxRecorder syncOutboxRecorder) {
        this.encounterRepository = encounterRepository;
        this.syncOutboxRecorder = syncOutboxRecorder;
    }

    @Override
    @Transactional
    // domain encounter class handles the business logic of cancelling an encounter
    public void cancel(UUID encounterId, Instant now) {
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new EncounterNotFoundException(encounterId));

        encounter.cancel(now);

        encounterRepository.save(encounter);

        syncOutboxRecorder.record(
                AggregateType.ENCOUNTER,
                encounterId,
                "EncounterCancelled",
                Map.of("endedAt", now.toString())
        );
    }
}