package com.matibabu.backend.exception;

import java.util.UUID;

/**
 * Raised when a department is requested in a context that belongs to a different facility
 * (e.g. starting an encounter on a department that is not owned by the local node).
 */
public class DepartmentFacilityMismatchException extends RuntimeException {

    public DepartmentFacilityMismatchException(UUID departmentId, UUID expectedFacilityId) {
        super(
                "Department " + departmentId
                        + " does not belong to the current facility " + expectedFacilityId
        );
    }
}
