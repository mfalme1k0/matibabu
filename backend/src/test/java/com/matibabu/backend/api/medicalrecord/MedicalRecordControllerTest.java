package com.matibabu.backend.api.medicalrecord;

import com.matibabu.backend.application.medicalrecord.AddAllergy;
import com.matibabu.backend.application.medicalrecord.AddClinicalObservation;
import com.matibabu.backend.application.medicalrecord.AddDiagnosis;
import com.matibabu.backend.application.medicalrecord.AddTreatment;
import com.matibabu.backend.application.medicalrecord.AddVital;
import com.matibabu.backend.application.medicalrecord.CreateMedicalRecordUseCase;
import com.matibabu.backend.application.medicalrecord.GetMedicalRecordUseCase;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.exception.MedicalRecordNotFoundException;
import com.matibabu.backend.domain.medicalrecord.VitalType;
import com.matibabu.backend.domain.medicalrecord.DiagnosisType;
import com.matibabu.backend.security.CustomUserDetails;
import com.matibabu.backend.security.entity.Clinician;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;



class MedicalRecordControllerTest {
    private CreateMedicalRecordUseCase createMedicalRecordUseCase;
    private GetMedicalRecordUseCase getMedicalRecordUseCase;
    private AddVital addVital;
    private AddClinicalObservation addClinicalObservation;
    private AddDiagnosis addDiagnosis;
    private AddTreatment addTreatment;
    private AddAllergy addAllergy;

    private MedicalRecordController controller;

    @BeforeEach
    void setUp() {
        createMedicalRecordUseCase = mock(CreateMedicalRecordUseCase.class);
        getMedicalRecordUseCase = mock(GetMedicalRecordUseCase.class);
        addVital = mock(AddVital.class);
        addClinicalObservation = mock(AddClinicalObservation.class);
        addDiagnosis = mock(AddDiagnosis.class);
        addTreatment = mock(AddTreatment.class);
        addAllergy = mock(AddAllergy.class);

        controller = new MedicalRecordController(
            createMedicalRecordUseCase,
            getMedicalRecordUseCase,
            addVital,
            addClinicalObservation,
            addDiagnosis,
            addTreatment,
            addAllergy
        );
    }


    
    @Test
    void create() {
        UUID encounterId = UUID.randomUUID();
        MedicalRecord medicalRecord = mock(MedicalRecord.class);

        when(createMedicalRecordUseCase.create(encounterId))
                .thenReturn(medicalRecord);

        MedicalRecordResponse response = controller.create(encounterId);

        assertNotNull(response);

        verify(createMedicalRecordUseCase).create(encounterId);
    }


    
    @Test
    void get() {
        UUID encounterId = UUID.randomUUID();
        MedicalRecord medicalRecord = mock(MedicalRecord.class);

        when(getMedicalRecordUseCase.getByEncounterId(encounterId))
                .thenReturn(Optional.of(medicalRecord));

        MedicalRecordResponse response = controller.get(encounterId);

        assertNotNull(response);

        verify(getMedicalRecordUseCase).getByEncounterId(encounterId);
    }


    @Test
    void addVital() {
        UUID encounterId = UUID.randomUUID();
        UUID medicalRecordId = UUID.randomUUID();

        MedicalRecord medicalRecord = mock(MedicalRecord.class);
        MedicalRecord updatedRecord = mock(MedicalRecord.class);

        VitalType type = VitalType.values()[0];

        when(getMedicalRecordUseCase.getByEncounterId(encounterId))
                .thenReturn(Optional.of(medicalRecord));
        when(medicalRecord.getId()).thenReturn(medicalRecordId);
        when(addVital.execute(
                medicalRecordId, type, "120", "mmHg"
        )).thenReturn(updatedRecord);

        var response = controller.addVital(
                encounterId, type, "120", "mmHg"
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        verify(addVital).execute(
                medicalRecordId, type, "120", "mmHg"
        );
    }

    
    @Test
    void addAllergy() {
        UUID encounterId = UUID.randomUUID();
        UUID medicalRecordId = UUID.randomUUID();

        MedicalRecord medicalRecord = mock(MedicalRecord.class);
        MedicalRecord updatedRecord = mock(MedicalRecord.class);

        when(getMedicalRecordUseCase.getByEncounterId(encounterId))
                .thenReturn(Optional.of(medicalRecord));

        when(medicalRecord.getId()).thenReturn(medicalRecordId);

        when(addAllergy.execute(
                medicalRecordId,
                "Penicillin",
                "Skin rash"
        )).thenReturn(updatedRecord);

        var response = controller.addAllergy(
                encounterId,
                "Penicillin",
                "Skin rash"
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        verify(addAllergy).execute(
                medicalRecordId,
                "Penicillin",
                "Skin rash"
        );
    }

    
    @Test
    void addAllergyShouldThrowWhenMedicalRecordDoesNotExist() {
        UUID encounterId = UUID.randomUUID();

        when(getMedicalRecordUseCase.getByEncounterId(encounterId))
                .thenReturn(Optional.empty());

        assertThrows(
                MedicalRecordNotFoundException.class,
                () -> controller.addAllergy(
                        encounterId,
                        "Penicillin",
                        "Skin rash"
                )
        );

        verify(addAllergy, never()).execute(
                any(UUID.class),
                anyString(),
                anyString()
        );
    }



    @Test
    void addObservation() {
        UUID encounterId = UUID.randomUUID();
        UUID medicalRecordId = UUID.randomUUID();

        MedicalRecord medicalRecord = mock(MedicalRecord.class);
        MedicalRecord updatedRecord = mock(MedicalRecord.class);

        when(getMedicalRecordUseCase.getByEncounterId(encounterId))
                .thenReturn(Optional.of(medicalRecord));
        when(medicalRecord.getId()).thenReturn(medicalRecordId);
        when(addClinicalObservation.execute(
                medicalRecordId, "Patient reports a headache"
        )).thenReturn(updatedRecord);

        var response = controller.addObservation(
                encounterId, "Patient reports a headache"
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        verify(addClinicalObservation).execute(
                medicalRecordId, "Patient reports a headache"
        );
    }

        @Test
    void addDiagnosis() {
        UUID encounterId = UUID.randomUUID();
        UUID medicalRecordId = UUID.randomUUID();

        MedicalRecord medicalRecord = mock(MedicalRecord.class);
        MedicalRecord updatedRecord = mock(MedicalRecord.class);

        DiagnosisType type = DiagnosisType.values()[0];

        when(getMedicalRecordUseCase.getByEncounterId(encounterId))
                .thenReturn(Optional.of(medicalRecord));
        when(medicalRecord.getId()).thenReturn(medicalRecordId);
        when(addDiagnosis.execute(
                medicalRecordId, "Malaria", type
        )).thenReturn(updatedRecord);

        var response = controller.addDiagnosis(
                encounterId, "Malaria", type
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        verify(addDiagnosis).execute(
                medicalRecordId, "Malaria", type
        );
    }


    @Test
    void addTreatment() {
        UUID encounterId = UUID.randomUUID();
        UUID medicalRecordId = UUID.randomUUID();
        UUID medicineId = UUID.randomUUID();
        UUID clinicianId = UUID.randomUUID();

        MedicalRecord record = mock(MedicalRecord.class);
        MedicalRecord updatedRecord = mock(MedicalRecord.class);
        Clinician clinician = mock(Clinician.class);
        CustomUserDetails principal = mock(CustomUserDetails.class);

        AddTreatmentRequest request = new AddTreatmentRequest(
                medicineId,
                "2",
                "tablets",
                "oral",
                "twice daily",
                5,
                "After meals"
        );

        when(getMedicalRecordUseCase.getByEncounterId(encounterId))
                .thenReturn(Optional.of(record));
        when(record.getId()).thenReturn(medicalRecordId);
        when(principal.getClinician()).thenReturn(clinician);
        when(clinician.getId()).thenReturn(clinicianId);
        when(addTreatment.execute(
                medicalRecordId,
                medicineId,
                clinicianId,
                "2",
                "tablets",
                "oral",
                "twice daily",
                5,
                "After meals"
        )).thenReturn(updatedRecord);

        ResponseEntity<MedicalRecordResponse> response =
                controller.addTreatment(encounterId, request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(addTreatment).execute(
                medicalRecordId,
                medicineId,
                clinicianId,
                "2",
                "tablets",
                "oral",
                "twice daily",
                5,
                "After meals"
        );
    }

    
}