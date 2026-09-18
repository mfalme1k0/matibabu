package com.matibabu.backend.exception;

import java.util.UUID;

public class DepartmentCodeAlreadyExistsException extends RuntimeException {
    public DepartmentCodeAlreadyExistsException(UUID facilityId, String code) {

        super("Department code" +  code + "already exists for facility:" + facilityId);
    }
}
