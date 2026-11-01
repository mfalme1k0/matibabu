package com.matibabu.backend.api.department;

import com.matibabu.backend.domain.department.Department;

import java.util.UUID;

public record DepartmentResponse(
        UUID id,
        UUID facilityId,
        String code,
        String name,
        boolean active
) {
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getFacilityId(),
                department.getCode(),
                department.getName(),
                department.isActive()
        );
    }
}
