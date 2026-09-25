package com.matibabu.backend.api.department;

import com.matibabu.backend.domain.department.Department;

import java.util.UUID;

public record DepartmentResponse(
        UUID Id,
        UUID facilityId,
        String Code,
        String name,
        boolean Active
) {
    public static DepartmentResponse from(Department department){
        return new DepartmentResponse(
                department.getId(),
                department.getFacilityId(),
                department.getCode(),
                department.getName(),
                department.isActive()
        );
    }
}