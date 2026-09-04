package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.ClinicalObservation;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddClinicalObservationTest {

    @Test
    void shouldAddClinicalObservationToMedicalRecord() {
        // Arrange
        MedicalRecordRepository repository =
                mock(MedicalRecordRepository.class);

        UUID medicalRecordId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();

        MedicalRecord medicalRecord =
                new MedicalRecord(patientId, encounterId);

        // Simulate finding the medical record in the repository.
        when(repository.findById(medicalRecordId))
                .thenReturn(Optional.of(medicalRecord));

        // Simulate successfully saving the updated record.
        when(repository.save(medicalRecord))
                .thenReturn(medicalRecord);

        AddClinicalObservation addClinicalObservation =
                new AddClinicalObservation(repository);

        String description =
                "Patient is alert and responsive";

        // Act
        MedicalRecord result =
                addClinicalObservation.execute(
                        medicalRecordId,
                        description
                );

        // Assert
        assertNotNull(result);

        // Verify that the observation was added to the record.
        assertEquals(1, result.getObservations().size());

        ClinicalObservation observation =
                result.getObservations().get(0);

        // Verify the observation contains the expected information.
        assertEquals(
                medicalRecordId,
                observation.getMedicalRecordId()
        );
        assertEquals(
                description,
                observation.getDescription()
        );
        assertNotNull(observation.getRecordedAt());

        // Verify that the observation uses UUID v7.
        assertEquals(7, observation.getId().version());

        // Verify that the updated medical record was persisted.
        verify(repository).save(medicalRecord);
    }

    @Test
    void shouldThrowExceptionWhenMedicalRecordDoesNotExist() {
        // Arrange
        MedicalRecordRepository repository =
                mock(MedicalRecordRepository.class);

        UUID medicalRecordId = UUID.randomUUID();

        // Simulate a missing medical record.
        when(repository.findById(medicalRecordId))
                .thenReturn(Optional.empty());

        AddClinicalObservation addClinicalObservation =
                new AddClinicalObservation(repository);

        // Act & Assert
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addClinicalObservation.execute(
                                medicalRecordId,
                                "Patient is alert"
                        )
                );

        // Verify that the expected error is returned.
        assertTrue(
                exception.getMessage()
                        .contains("Medical record not found")
        );

        // Nothing should be saved when the record does not exist.
        verify(repository, never()).save(any());
    }
}