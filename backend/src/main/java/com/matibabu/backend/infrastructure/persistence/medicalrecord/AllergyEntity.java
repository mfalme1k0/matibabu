package com.matibabu.backend.infrastructure.persistence.medicalrecord;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "medical_record_allergies")
public class AllergyEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 36)
    private UUID id;

    @Column(name = "medical_record_id", nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID medicalRecordId;

    @Column(nullable = false, length = 255)
    private String allergen;

    @Column(nullable = false, length = 1000)
    private String reaction;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected AllergyEntity() {
    }

    public AllergyEntity(
        UUID id,
        UUID medicalRecordId,
        String allergen,
        String reaction,
        Instant recordedAt
    ) {
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