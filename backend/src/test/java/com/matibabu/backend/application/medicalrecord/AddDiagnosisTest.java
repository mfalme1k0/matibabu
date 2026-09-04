package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.Diagnosis;
import com.matibabu.backend.domain.medicalrecord.DiagnosisType;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddDiagnosisTest {

    @Test
    void shouldAddDiagnosisToMedicalRecord() {
        // Arrange
        MedicalRecordRepository repository =
                mock(MedicalRecordRepository.class);

        UUID medicalRecordId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();

        MedicalRecord medicalRecord =
                new MedicalRecord(patientId, encounterId);

        when(repository.findById(medicalRecordId))
                .thenReturn(java.util.Optional.of(medicalRecord));

        when(repository.save(medicalRecord))
                .thenReturn(medicalRecord);

        AddDiagnosis addDiagnosis =
                new AddDiagnosis(repository);

        String description = "Malaria";
        DiagnosisType type = DiagnosisType.CONFIRMED;

        // Act
        MedicalRecord result =
                addDiagnosis.execute(
                        medicalRecordId,
                        description,
                        type
                );

        // Assert
        assertNotNull(result);

        // Verify that the diagnosis was added to the medical record.
        assertEquals(1, result.getDiagnoses().size());

        Diagnosis diagnosis =
                result.getDiagnoses().get(0);

        assertEquals(description, diagnosis.getDescription());
        assertEquals(type, diagnosis.getType());
        assertEquals(
                medicalRecordId,
                diagnosis.getMedicalRecordId()
        );

        // Verify that the diagnosis uses UUID v7.
        assertEquals(7, diagnosis.getId().version());

        // Verify that the updated medical record was saved.
        verify(repository).save(medicalRecord);
    }

    @Test
    void shouldThrowExceptionWhenMedicalRecordDoesNotExist() {
        // Arrange
        MedicalRecordRepository repository =
                mock(MedicalRecordRepository.class);

        UUID medicalRecordId = UUID.randomUUID();

        when(repository.findById(medicalRecordId))
                .thenReturn(java.util.Optional.empty());

        AddDiagnosis addDiagnosis =
                new AddDiagnosis(repository);

        // Act & Assert
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addDiagnosis.execute(
                                medicalRecordId,
                                "Malaria",
                                DiagnosisType.CONFIRMED
                        )
                );

        // Verify that the correct error is reported.
        assertTrue(
                exception.getMessage()
                        .contains("Medical record not found")
        );

        // Verify that nothing was saved because the record
        // could not be found.
        verify(repository, never()).save(any());
    }
}