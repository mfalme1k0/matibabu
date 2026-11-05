package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.Allergy;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AddAllergyTest {
    
    @Test
    void shouldAddAllergyToExistingMedicalRecord() {
        // Arrange
        MedicalRecordRepository repository = mock(MedicalRecordRepository.class);
        AddAllergy addAllergy = new AddAllergy(repository);

        UUID medicalRecordId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();

        MedicalRecord medicalRecord =
            new MedicalRecord(patientId, encounterId);

        when(repository.findById(medicalRecordId))
            .thenReturn(Optional.of(medicalRecord));

        when(repository.save(any(MedicalRecord.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MedicalRecord result = addAllergy.execute(
            medicalRecordId,
            "Penicillin",
            "Skin rash"
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getAllergies().size());

        Allergy allergy = result.getAllergies().get(0);
        assertEquals("Penicillin", allergy.getAllergen());
        assertEquals("Skin rash", allergy.getReaction());

        verify(repository).findById(medicalRecordId);
        verify(repository).save(medicalRecord);
    }
    
    @Test
    void shouldThrowExceptionWhenMedicalRecordDoesNotExist() {
        // Arrange
        MedicalRecordRepository repository = mock(MedicalRecordRepository.class);
        AddAllergy addAllergy = new AddAllergy(repository);

        UUID medicalRecordId = UUID.randomUUID();

        when(repository.findById(medicalRecordId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            IllegalArgumentException.class,
            () -> addAllergy.execute(
                medicalRecordId,
                "Penicillin",
                "Skin rash"
            )
        );

        verify(repository).findById(medicalRecordId);
        verify(repository, never()).save(any(MedicalRecord.class));
    }

}
