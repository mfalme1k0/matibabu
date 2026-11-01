package com.matibabu.backend.api.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateDepartmentRequest(
        @NotNull UUID facilityId,
        @NotBlank String code,
        @NotBlank String name
) {
}
