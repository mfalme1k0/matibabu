package com.matibabu.backend.domain.medicalrecord;

import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class MedicalRecord {

    private UUID uuid;

    // Identifies the patient this medical record belongs to.
    private UUID patientId;

    // Identifies the encounter during which this record was created.
    private UUID encounterId;

    private Instant createdAt;

    private final List<Vital> vitals = new ArrayList<>();
    private final List<ClinicalObservation> observations = new ArrayList<>();
    private final List<Diagnosis> diagnoses = new ArrayList<>();
    private final List<Treatment> treatments = new ArrayList<>();
    private final List<Allergy> allergies = new ArrayList<>();


    /*
     * Creates a new medical record for a specific patient
     * and encounter.
     */
    public MedicalRecord(UUID patientId, UUID encounterId) {

        this.uuid = UuidCreator.getTimeOrderedEpoch();
        this.patientId = patientId;
        this.encounterId = encounterId;
        this.createdAt = Instant.now();
    }


    /*
     * Reconstruct an existing medical record from persisted data.
     */
    public static MedicalRecord reconstitute(
        UUID uuid,
        UUID patientId,
        UUID encounterId,
        Instant createdAt
    ) {

        MedicalRecord medicalRecord =
        new MedicalRecord(patientId, encounterId);

        medicalRecord.uuid = uuid;
        medicalRecord.createdAt = createdAt;

        return medicalRecord;
    }


    // Vitals

    public void addVital(Vital vital) {
        vitals.add(vital);
    }

    public List<Vital> getVitals() {
        return List.copyOf(vitals);
    }


    // Clinical observations

    public void addObservation(ClinicalObservation observation) {
        observations.add(observation);
    }

    public List<ClinicalObservation> getObservations() {
        return List.copyOf(observations);
    }


    // Diagnoses

    public void addDiagnosis(Diagnosis diagnosis) {
        diagnoses.add(diagnosis);
    }

    public List<Diagnosis> getDiagnoses() {
        return List.copyOf(diagnoses);
    }


    // Treatments

    public void addTreatment(Treatment treatment) {
        treatments.add(treatment);
    }

    public List<Treatment> getTreatments() {
        return List.copyOf(treatments);
    }


    // Allergies
    public void addAllergy(Allergy allergy) {
        allergies.add(allergy);
    }

    public List<Allergy> getAllergies() {
        return List.copyOf(allergies);
    }


    // Basic information

    public UUID getId() {
        return uuid;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getEncounterId() {
        return encounterId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}