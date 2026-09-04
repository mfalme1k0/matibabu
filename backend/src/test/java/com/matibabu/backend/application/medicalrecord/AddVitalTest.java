package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import com.matibabu.backend.domain.medicalrecord.Vital;
import com.matibabu.backend.domain.medicalrecord.VitalType;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddVitalTest {


    @Test
    void shouldAddVitalToMedicalRecord() {

        // Create an ID representing the medical record we want to update.
        UUID medicalRecordId = UUID.randomUUID();

        // Create a medical record that will be returned by the repository.
        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        // Mock the repository so this test does not depend on a database.
        MedicalRecordRepository medicalRecordRepository =
                mock(MedicalRecordRepository.class);

        /*
         * When the service looks for the medical record,
         * pretend that the record exists.
         */
        when(medicalRecordRepository.findById(medicalRecordId))
                .thenReturn(Optional.of(medicalRecord));

        /*
         * When the service saves the updated medical record,
         * return the same object that was passed to save().
         */
        when(medicalRecordRepository.save(medicalRecord))
                .thenReturn(medicalRecord);

        // Create the service under test and inject the mocked repository.
        AddVital service =
                new AddVital(medicalRecordRepository);

        /*
         * Execute the operation.
         *
         * The service should:
         * 1. Find the medical record.
         * 2. Create a Vital.
         * 3. Add the Vital to the medical record.
         * 4. Save the updated medical record.
         */
        MedicalRecord result =
                service.execute(
                        medicalRecordId,
                        VitalType.TEMPERATURE,
                        "37.5",
                        "°C"
                );

        // The service should return the updated medical record.
        assertNotNull(result);

        // The medical record should now contain exactly one vital.
        assertEquals(1, result.getVitals().size());

        // Retrieve the vital that was added so we can verify its data.
        Vital vital = result.getVitals().get(0);

        // The vital should belong to the medical record being updated.
        assertEquals(medicalRecordId, vital.getMedicalRecordId());

        // Verify that the requested vital type was stored.
        assertEquals(VitalType.TEMPERATURE, vital.getType());

        // Verify the value supplied to the service.
        assertEquals("37.5", vital.getValue());

        // Verify the unit supplied to the service.
        assertEquals("°C", vital.getUnit());

        // A new Vital should have its own generated ID.
        assertNotNull(vital.getId());

        // A new Vital should have a recording timestamp.
        assertNotNull(vital.getRecordedAt());

        /*
         * Verify that the service actually looked up
         * the medical record using the supplied ID.
         */
        verify(medicalRecordRepository)
                .findById(medicalRecordId);

        /*
         * Verify that the updated medical record was persisted.
         */
        verify(medicalRecordRepository)
                .save(medicalRecord);
    }

    @Test
    void shouldThrowExceptionWhenMedicalRecordDoesNotExist() {

        // Create an ID for a medical record that does not exist.
        UUID medicalRecordId = UUID.randomUUID();

        // Mock the repository so the lookup returns no record.
        MedicalRecordRepository medicalRecordRepository =
                mock(MedicalRecordRepository.class);

        /*
         * Simulate the repository being unable to find
         * the requested medical record.
         */
        when(medicalRecordRepository.findById(medicalRecordId))
                .thenReturn(Optional.empty());

        // Create the service using the mocked repository.
        AddVital service =
                new AddVital(medicalRecordRepository);

        /*
         * The service should reject the operation because
         * there is no medical record to which the vital can be added.
         */
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.execute(
                                medicalRecordId,
                                VitalType.TEMPERATURE,
                                "37.5",
                                "°C"
                        )
                );

        // Verify that the exception contains the expected message.
        assertEquals(
                "Medical record not found: " + medicalRecordId,
                exception.getMessage()
        );

        // Verify that the service attempted to find the record.
        verify(medicalRecordRepository)
                .findById(medicalRecordId);

        /*
         * Since the medical record does not exist,
         * nothing should be saved.
         */
        verify(medicalRecordRepository, never())
                .save(any(MedicalRecord.class));
    }


}
