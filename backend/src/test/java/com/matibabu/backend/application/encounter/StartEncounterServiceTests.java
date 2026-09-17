package com.matibabu.backend.application.encounter;

import com.matibabu.backend.config.NodeIdentity;
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

class StartEncounterServiceTest {


    /*
     * Simple in-memory implementation of EncounterRepository.
     *
     * This allows us to test the application service without
     * connecting to a database.
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
     * Verifies that starting an encounter:
     *
     * 1. Creates an Encounter.
     * 2. Generates an ID.
     * 3. Associates it with the correct patient.
     * 4. Associates it with the correct attending clinician.
     * 5. Starts it as ACTIVE.
     * 6. Preserves the supplied start time.
     * 7. Saves it through the repository.
     */
    @Test
    void shouldStartAndSaveEncounter() {

        UUID patientId = UUID.randomUUID();
        UUID attendingClinicianId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();

        Instant startedAt =
                Instant.parse("2026-08-20T10:00:00Z");

        // Create the repository used by the application service.
        InMemoryEncounterRepository repository =
                new InMemoryEncounterRepository();

        // Create the application service.
        SyncOutboxRecorder syncOutboxRecorder = mock(SyncOutboxRecorder.class);
        StartEncounterService service =
                new StartEncounterService(repository, new NodeIdentity("facility", facilityId.toString()), syncOutboxRecorder);

        // Execute the start encounter use case.
        Encounter encounter =
                service.start(
                        patientId,
                        attendingClinicianId,
                        startedAt
                );

        // The encounter should have a generated ID.
        assertNotNull(encounter.getId());

        // The correct patient should be associated with the encounter.
        assertEquals(
                patientId,
                encounter.getPatientId()
        );

        // The correct clinician should be associated with the encounter.
        assertEquals(
                attendingClinicianId,
                encounter.getAttendingClinicianId()
        );

        // The configured facility should be associated with the encounter.
        assertEquals(
                facilityId,
                encounter.getFacilityId()
        );

        // The encounter should start as ACTIVE.
        assertEquals(
                EncounterStatus.ACTIVE,
                encounter.getStatus()
        );

        // The supplied start time should be preserved.
        assertEquals(
                startedAt,
                encounter.getStartedAt()
        );

        // An active encounter should not have an end time.
        assertNull(encounter.getEndedAt());

        // Verify that the service actually saved the encounter.
        Encounter savedEncounter =
                repository.findById(encounter.getId())
                        .orElseThrow();

        assertEquals(
                encounter.getId(),
                savedEncounter.getId()
        );

        // The service should have recorded a sync outbox entry.
        verify(syncOutboxRecorder).record(
                eq(AggregateType.ENCOUNTER),
                eq(encounter.getId()),
                eq("EncounterStarted"),
                any()
        );
    }


}
