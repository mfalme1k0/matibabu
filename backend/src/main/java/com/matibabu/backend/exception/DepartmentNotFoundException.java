package com.matibabu.backend.exception;

import java.util.UUID;

public class DepartmentNotFoundException extends RuntimeException {
    public DepartmentNotFoundException(UUID Id) {
        super( "Department" + Id + " not found!");
    }
}
