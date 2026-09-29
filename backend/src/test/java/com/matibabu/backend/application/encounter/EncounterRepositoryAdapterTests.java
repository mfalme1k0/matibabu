package com.matibabu.backend.application.encounter;


import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.domain.encounter.EncounterStatus;
import com.matibabu.backend.infrastructure.persistence.encounter.EncounterEntity;
import com.matibabu.backend.infrastructure.persistence.encounter.EncounterMapperImpl;
import com.matibabu.backend.infrastructure.persistence.encounter.EncounterRepositoryAdapter;
import com.matibabu.backend.infrastructure.persistence.encounter.SpringDataEncounterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        EncounterRepositoryAdapter.class,
        EncounterMapperImpl.class
})
class EncounterRepositoryAdapterTests {

    @Autowired
    private EncounterRepository encounterRepository;

    @Autowired
    private SpringDataEncounterRepository jpaRepository;

    @Test
    void shouldSaveAndFindEncounter() {

        UUID patientId = UUID.randomUUID();

        UUID departmentId = UUID.randomUUID();

        UUID attendingClinicianId =
                UUID.randomUUID();

        UUID facilityId =
                UUID.randomUUID();

        Instant startedAt =
                Instant.parse("2026-08-20T10:00:00Z");



// Create an active encounter.
        Encounter encounter =
                Encounter.start(
                        patientId,
                        attendingClinicianId,
                        facilityId,
                        departmentId,
                        startedAt
                );

        // Save through the DOMAIN repository interface.
        Encounter saved =
                encounterRepository.save(encounter);

        // Verify the saved domain object.
        assertNotNull(saved);
        assertEquals(encounter.getId(), saved.getId());
        assertEquals(patientId, saved.getPatientId());
        assertEquals(
                EncounterStatus.ACTIVE,
                saved.getStatus()
        );
        assertEquals(
                startedAt,
                saved.getStartedAt()
        );
        assertNull(saved.getEndedAt());

        // Verify that the data actually reached the database.
        EncounterEntity entity =
                jpaRepository.findById(encounter.getId())
                        .orElseThrow();

        assertEquals(
                encounter.getId(),
                entity.getId()
        );

        assertEquals(
                patientId,
                entity.getPatientId()
        );

        assertEquals(
                startedAt,
                entity.getStartedAt()
        );

        assertEquals(
                EncounterStatus.ACTIVE,
                entity.getStatus()
        );

        assertNull(entity.getEndedAt());

        // Now retrieve it through the DOMAIN repository.
        Encounter retrieved =
                encounterRepository.findById(encounter.getId())
                        .orElseThrow();

        // Verify that persistence → domain reconstruction works.
        assertEquals(
                encounter.getId(),
                retrieved.getId()
        );

        assertEquals(
                patientId,
                retrieved.getPatientId()
        );

        assertEquals(
                startedAt,
                retrieved.getStartedAt()
        );

        assertEquals(
                EncounterStatus.ACTIVE,
                retrieved.getStatus()
        );

        assertNull(retrieved.getEndedAt());
    }
}