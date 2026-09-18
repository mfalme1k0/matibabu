package com.matibabu.backend.domain.department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface DepartmentRepository {

    Department save(Department department);

    Optional<Department> findById(UUID id);

    List<Department> findByFacilityId(UUID facilityId);

    List<Department> findAllActive(UUID facilityId);

    Optional<Department> findByFacilityIdAndCode(
            UUID facilityId,
            String code
    );

    boolean existsByFacilityIdAndCode(
            UUID facilityId,
            String code
    );
}