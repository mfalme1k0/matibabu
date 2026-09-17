package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.exception.EncounterNotFoundException;
import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
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

        SyncOutboxRecorder syncOutboxRecorder = mock(SyncOutboxRecorder.class);
        CreateMedicalRecordService service =
                new CreateMedicalRecordService(
                        medicalRecordRepository,
                        encounterRepository,
                        syncOutboxRecorder
                );

        MedicalRecord result = service.create(encounterId);

        assertNotNull(result);
        assertEquals(patientId, result.getPatientId());
        assertEquals(encounterId, result.getEncounterId());

        verify(encounterRepository).findById(encounterId);
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
        verify(syncOutboxRecorder).record(
                eq(AggregateType.MEDICAL_RECORD),
                eq(result.getId()),
                eq("MedicalRecordCreated"),
                any()
        );
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
                        encounterRepository,
                        mock(SyncOutboxRecorder.class)
                );

        assertThrows(
                EncounterNotFoundException.class,
                () -> service.create(encounterId)
        );

        verify(encounterRepository).findById(encounterId);
        verifyNoInteractions(medicalRecordRepository);
    }


}
