package com.matibabu.backend.domain.medicalrecord;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MedicalRecordTest {

    @Test
    void shouldCreateMedicalRecord() {
        UUID patientId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();

        MedicalRecord medicalRecord =
                new MedicalRecord(patientId, encounterId);

        assertNotNull(medicalRecord.getId());
        assertEquals(patientId, medicalRecord.getPatientId());
        assertEquals(encounterId, medicalRecord.getEncounterId());
        assertNotNull(medicalRecord.getCreatedAt());
    }

    @Test
    void shouldReconstituteMedicalRecord() {
        UUID id = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        MedicalRecord medicalRecord =
                MedicalRecord.reconstitute(
                        id,
                        patientId,
                        encounterId,
                        createdAt
                );

        assertEquals(id, medicalRecord.getId());
        assertEquals(patientId, medicalRecord.getPatientId());
        assertEquals(encounterId, medicalRecord.getEncounterId());
        assertEquals(createdAt, medicalRecord.getCreatedAt());
    }

    @Test
    void shouldAddVital() {
        UUID patientId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();

        MedicalRecord medicalRecord =
                new MedicalRecord(patientId, encounterId);

        Vital vital = new Vital(
                medicalRecord.getId(),
                VitalType.TEMPERATURE,
                "37.5",
                "°C"
        );

        medicalRecord.addVital(vital);

        assertEquals(1, medicalRecord.getVitals().size());
        assertEquals(vital, medicalRecord.getVitals().get(0));
    }

    @Test
    void shouldReturnVitals() {
        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        assertNotNull(medicalRecord.getVitals());
        assertTrue(medicalRecord.getVitals().isEmpty());
    }

    @Test
    void shouldAddObservation() {
        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        ClinicalObservation observation =
                new ClinicalObservation(
                        medicalRecord.getId(),
                        "Patient reports headache"
                );

        medicalRecord.addObservation(observation);

        assertEquals(1, medicalRecord.getObservations().size());
        assertEquals(observation, medicalRecord.getObservations().get(0));
    }

    @Test
    void shouldReturnObservations() {
        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        assertNotNull(medicalRecord.getObservations());
        assertTrue(medicalRecord.getObservations().isEmpty());
    }

    @Test
    void shouldAddDiagnosis() {
        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        Diagnosis diagnosis =
                new Diagnosis(
                        medicalRecord.getId(),
                        "Malaria",
                        DiagnosisType.CONFIRMED
                );

        medicalRecord.addDiagnosis(diagnosis);

        assertEquals(1, medicalRecord.getDiagnoses().size());
        assertEquals(diagnosis, medicalRecord.getDiagnoses().get(0));
    }

    @Test
    void shouldReturnDiagnoses() {
        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        assertNotNull(medicalRecord.getDiagnoses());
        assertTrue(medicalRecord.getDiagnoses().isEmpty());
    }

    @Test
    void shouldAddTreatment() {
        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        Treatment treatment =
                new Treatment(
                        medicalRecord.getId(),
                        "Prescribed medication"
                );

        medicalRecord.addTreatment(treatment);

        assertEquals(1, medicalRecord.getTreatments().size());
        assertEquals(treatment, medicalRecord.getTreatments().get(0));
    }

    @Test
    void shouldReturnTreatments() {
        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        assertNotNull(medicalRecord.getTreatments());
        assertTrue(medicalRecord.getTreatments().isEmpty());
    }

    @Test
    void shouldReturnId() {
        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        assertNotNull(medicalRecord.getId());
    }

    @Test
    void shouldReturnPatientId() {
        UUID patientId = UUID.randomUUID();

        MedicalRecord medicalRecord =
                new MedicalRecord(
                        patientId,
                        UUID.randomUUID()
                );

        assertEquals(patientId, medicalRecord.getPatientId());
    }

    @Test
    void shouldReturnEncounterId() {
        UUID encounterId = UUID.randomUUID();

        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        encounterId
                );

        assertEquals(encounterId, medicalRecord.getEncounterId());
    }

    @Test
    void shouldReturnCreatedAt() {
        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        assertNotNull(medicalRecord.getCreatedAt());
    }

    @Test
    void shouldReturnUnmodifiableVitals() {
        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        List<Vital> vitals = medicalRecord.getVitals();

        assertThrows(
                UnsupportedOperationException.class,
                () -> vitals.add(null)
        );
    }

    @Test
    void shouldReturnUnmodifiableObservations() {
        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        List<ClinicalObservation> observations =
                medicalRecord.getObservations();

        assertThrows(
                UnsupportedOperationException.class,
                () -> observations.add(null)
        );
    }

    @Test
    void shouldReturnUnmodifiableDiagnoses() {
        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        List<Diagnosis> diagnoses =
                medicalRecord.getDiagnoses();

        assertThrows(
                UnsupportedOperationException.class,
                () -> diagnoses.add(null)
        );
    }

    @Test
    void shouldReturnUnmodifiableTreatments() {
        MedicalRecord medicalRecord =
                new MedicalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        List<Treatment> treatments =
                medicalRecord.getTreatments();

        assertThrows(
                UnsupportedOperationException.class,
                () -> treatments.add(null)
        );
    }
}