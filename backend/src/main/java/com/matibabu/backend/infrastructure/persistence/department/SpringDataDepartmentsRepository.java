package com.matibabu.backend.infrastructure.persistence.department;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataDepartmentsRepository
        extends JpaRepository<DepartmentEntity, UUID> {

    List<DepartmentEntity> findByFacilityId(UUID facilityId);

    List<DepartmentEntity> findByFacilityIdAndActiveTrue(UUID facilityId);

    Optional<DepartmentEntity> findByFacilityIdAndCode(
            UUID facilityId,
            String code
    );

    boolean existsByFacilityIdAndCode(
            UUID facilityId,
            String code
    );
}