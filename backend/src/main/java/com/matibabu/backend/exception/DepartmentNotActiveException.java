package com.matibabu.backend.exception;

import java.util.UUID;

/**
 * Raised when a clinical operation targets a department that has been deactivated.
 */
public class DepartmentNotActiveException extends RuntimeException {

    public DepartmentNotActiveException(UUID departmentId) {
        super("Department " + departmentId + " is not active");
    }
}
