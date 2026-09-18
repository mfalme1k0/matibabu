package com.matibabu.backend.application.department;

import com.matibabu.backend.domain.department.Department;

import java.util.UUID;

public interface CreateDepartmentUseCase {
    Department Create(UUID facilityId, String code, String name);
}
