package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.ClinicalObservation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClinicalObservationTest {

    @Test
    void shouldCreateClinicalObservation() {
        // Arrange
        UUID medicalRecordId = UUID.randomUUID();
        String description = "Patient reports chest pain";

        // Act
        ClinicalObservation observation =
                new ClinicalObservation(medicalRecordId, description);

        // Assert
        assertNotNull(observation.getId());
        assertEquals(medicalRecordId, observation.getMedicalRecordId());
        assertEquals(description, observation.getDescription());
        assertNotNull(observation.getRecordedAt());
    }

    @Test
    void shouldGenerateUuidV7ForClinicalObservation() {
        // Arrange
        UUID medicalRecordId = UUID.randomUUID();

        // Act
        ClinicalObservation observation =
                new ClinicalObservation(
                        medicalRecordId,
                        "Patient is alert and responsive"
                );

        // Assert
        assertEquals(7, observation.getId().version());
    }
}