package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetMedicalRecordServiceTest {


    @Test
    void shouldReturnMedicalRecordWhenItExists() {
        UUID encounterId = UUID.randomUUID();
        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        encounterId
                );

        MedicalRecordRepository medicalRecordRepository =
                mock(MedicalRecordRepository.class);

        when(medicalRecordRepository.findByEncounterId(encounterId))
                .thenReturn(Optional.of(medicalRecord));

        GetMedicalRecordService service =
                new GetMedicalRecordService(medicalRecordRepository);

        Optional<MedicalRecord> result =
                service.getByEncounterId(encounterId);

        assertTrue(result.isPresent());
        assertEquals(medicalRecord, result.get());

        verify(medicalRecordRepository)
                .findByEncounterId(encounterId);
    }

    @Test
    void shouldReturnEmptyWhenMedicalRecordDoesNotExist() {
        UUID encounterId = UUID.randomUUID();

        MedicalRecordRepository medicalRecordRepository =
                mock(MedicalRecordRepository.class);

        when(medicalRecordRepository.findByEncounterId(encounterId))
                .thenReturn(Optional.empty());

        GetMedicalRecordService service =
                new GetMedicalRecordService(medicalRecordRepository);

        Optional<MedicalRecord> result =
                service.getByEncounterId(encounterId);

        assertTrue(result.isEmpty());

        verify(medicalRecordRepository)
                .findByEncounterId(encounterId);
    }


}
