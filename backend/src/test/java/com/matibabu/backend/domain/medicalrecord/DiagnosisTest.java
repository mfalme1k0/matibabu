package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.Diagnosis;
import com.matibabu.backend.domain.medicalrecord.DiagnosisType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DiagnosisTest {

    @Test
    void shouldCreateDiagnosis() {
        // Arrange
        UUID medicalRecordId = UUID.randomUUID();
        String description = "Malaria";
        DiagnosisType type = DiagnosisType.CONFIRMED;

        // Act
        Diagnosis diagnosis =
                new Diagnosis(
                        medicalRecordId,
                        description,
                        type
                );

        // Assert
        assertNotNull(diagnosis.getId());
        assertEquals(medicalRecordId, diagnosis.getMedicalRecordId());
        assertEquals(description, diagnosis.getDescription());
        assertEquals(type, diagnosis.getType());
        assertNotNull(diagnosis.getRecordedAt());
    }

    @Test
    void shouldGenerateUuidV7ForDiagnosis() {
        // Arrange
        UUID medicalRecordId = UUID.randomUUID();

        // Act
        Diagnosis diagnosis =
                new Diagnosis(
                        medicalRecordId,
                        "Malaria",
                        DiagnosisType.CONFIRMED
                );

        // Assert
        assertEquals(7, diagnosis.getId().version());
    }
}