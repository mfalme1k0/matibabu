package com.matibabu.backend.infrastructure.persistence.medicalrecord;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataAllergiesRepository extends JpaRepository<AllergyEntity, UUID> {

    List<AllergyEntity> findByMedicalRecordId(UUID medicalRecordId);

    void deleteByMedicalRecordId(UUID medicalRecordId);
}
