package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.application.encounter.EncounterNotFoundException;
import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateMedicalRecordServiceTest {


    @Test
    void shouldCreateMedicalRecordForExistingEncounter() {
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        EncounterRepository encounterRepository =
                mock(EncounterRepository.class);

        MedicalRecordRepository medicalRecordRepository =
                mock(MedicalRecordRepository.class);

        Encounter encounter = mock(Encounter.class);

        when(encounterRepository.findById(encounterId))
                .thenReturn(Optional.of(encounter));

        when(encounter.getPatientId())
                .thenReturn(patientId);

        when(medicalRecordRepository.save(any(MedicalRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateMedicalRecordService service =
                new CreateMedicalRecordService(
                        medicalRecordRepository,
                        encounterRepository
                );

        MedicalRecord result = service.create(encounterId);

        assertNotNull(result);
        assertEquals(patientId, result.getPatientId());
        assertEquals(encounterId, result.getEncounterId());

        verify(encounterRepository).findById(encounterId);
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }

    @Test
    void shouldThrowExceptionWhenEncounterDoesNotExist() {
        UUID encounterId = UUID.randomUUID();

        EncounterRepository encounterRepository =
                mock(EncounterRepository.class);

        MedicalRecordRepository medicalRecordRepository =
                mock(MedicalRecordRepository.class);

        when(encounterRepository.findById(encounterId))
                .thenReturn(Optional.empty());

        CreateMedicalRecordService service =
                new CreateMedicalRecordService(
                        medicalRecordRepository,
                        encounterRepository
                );

        assertThrows(
                EncounterNotFoundException.class,
                () -> service.create(encounterId)
        );

        verify(encounterRepository).findById(encounterId);
        verifyNoInteractions(medicalRecordRepository);
    }


}
