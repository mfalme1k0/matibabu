package com.matibabu.backend.domain.medicalrecord;

import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.util.UUID;

public class Allergy {

    private UUID id;
    private UUID medicalRecordId;
    private String allergen;
    private String reaction;
    private Instant recordedAt;

    public Allergy(
        UUID medicalRecordId,
        String allergen,
        String reaction
    ) {
        this.id = UuidCreator.getTimeOrderedEpoch();
        this.medicalRecordId = medicalRecordId;
        this.allergen = allergen;
        this.reaction = reaction;
        this.recordedAt = Instant.now();
    }
    
    public static Allergy reconstitute(
        UUID id,
        UUID medicalRecordId,
        String allergen,
        String reaction,
        Instant recordedAt
    ) {
        return new Allergy(id, medicalRecordId, allergen, reaction, recordedAt);
    }

    private Allergy( UUID id,UUID medicalRecordId,String allergen,String reaction,Instant recordedAt) {
        this.id = id;
        this.medicalRecordId = medicalRecordId;
        this.allergen = allergen;
        this.reaction = reaction;
        this.recordedAt = recordedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMedicalRecordId() {
        return medicalRecordId;
    }

    public String getAllergen() {
        return allergen;
    }

    public String getReaction() {
        return reaction;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}