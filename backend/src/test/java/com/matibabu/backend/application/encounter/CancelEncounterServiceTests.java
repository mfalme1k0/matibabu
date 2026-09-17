package com.matibabu.backend.application.encounter;

import com.matibabu.backend.exception.EncounterNotFoundException;
import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.domain.encounter.EncounterStatus;
import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CancelEncounterServiceTest {

    /*
     * Simple in-memory repository used to test the
     * application service without a DB.
     */
    private static class InMemoryEncounterRepository
            implements EncounterRepository {

        private final Map<UUID, Encounter> encounters = new HashMap<>();

        @Override
        public Encounter save(Encounter encounter) {
            encounters.put(encounter.getId(), encounter);
            return encounter;
        }

        @Override
        public Optional<Encounter> findById(UUID id) {
            return Optional.ofNullable(encounters.get(id));
        }
    }

    /*
     * Verifies the normal cancellation workflow:
     *
     * ACTIVE → CANCELLED
     *
     * The service should:
     * - find the encounter
     * - cancel it through the domain
     * - save the updated encounter
     */
    @Test
    void shouldCancelExistingEncounter() {

        UUID patientId = UUID.randomUUID();

        UUID attendingClinicianId =
                UUID.randomUUID();

        UUID facilityId =
                UUID.randomUUID();

        Instant startedAt =
                Instant.parse("2026-08-20T10:00:00Z");

        Instant cancelledAt =
                Instant.parse("2026-08-20T11:00:00Z");

        InMemoryEncounterRepository repository =
                new InMemoryEncounterRepository();

        // Create an active encounter.
        Encounter encounter =
                Encounter.start(patientId, attendingClinicianId, facilityId, startedAt);

        // Persist it before executing the use case.
        repository.save(encounter);

        // Create the application service.
        SyncOutboxRecorder syncOutboxRecorder = mock(SyncOutboxRecorder.class);
        CancelEncounterService service =
                new CancelEncounterService(repository, syncOutboxRecorder);

        // Execute the cancellation use case.
        service.cancel(
                encounter.getId(),
                cancelledAt
        );

        // The service should have recorded a sync outbox entry.
        verify(syncOutboxRecorder).record(
                eq(AggregateType.ENCOUNTER),
                eq(encounter.getId()),
                eq("EncounterCancelled"),
                any()
        );

        // The encounter should now be CANCELLED.
        assertEquals(
                EncounterStatus.CANCELLED,
                encounter.getStatus()
        );

        // The cancellation time should be recorded.
        assertEquals(
                cancelledAt,
                encounter.getEndedAt()
        );

        // Verify that the updated encounter was saved.
        Encounter savedEncounter =
                repository.findById(encounter.getId())
                        .orElseThrow();

        assertEquals(
                EncounterStatus.CANCELLED,
                savedEncounter.getStatus()
        );

        assertEquals(
                cancelledAt,
                savedEncounter.getEndedAt()
        );
    }

    /*
     * Cancelling an encounter that does not exist should
     * result in EncounterNotFoundException.
     */
    @Test
    void shouldThrowWhenEncounterDoesNotExist() {

        InMemoryEncounterRepository repository =
                new InMemoryEncounterRepository();

        CancelEncounterService service =
                new CancelEncounterService(repository, mock(SyncOutboxRecorder.class));

        UUID encounterId = UUID.randomUUID();

        Instant cancelledAt =
                Instant.parse("2026-08-20T11:00:00Z");

        assertThrows(
                EncounterNotFoundException.class,
                () -> service.cancel(
                        encounterId,
                        cancelledAt
                )
        );
    }
}