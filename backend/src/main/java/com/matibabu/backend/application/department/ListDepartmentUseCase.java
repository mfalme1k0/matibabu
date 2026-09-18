
package com.matibabu.backend.application.department;

import com.matibabu.backend.domain.department.Department;

import java.util.List;
import java.util.UUID;

public interface ListDepartmentUseCase {

    Department getById(UUID id);

    List<Department> findByFacilityId(UUID facilityId);

    List<Department> findAllActive(UUID facilityId);
}

