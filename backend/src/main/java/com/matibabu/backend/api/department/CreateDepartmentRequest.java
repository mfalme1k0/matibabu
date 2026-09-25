package com.matibabu.backend.api.department;

import java.util.UUID;

public record CreateDepartmentRequest(

        UUID facilityId,
        String code,
        String name
) {
}