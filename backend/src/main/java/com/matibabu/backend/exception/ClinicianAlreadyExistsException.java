package com.matibabu.backend.exception;


public class ClinicianAlreadyExistsException extends RuntimeException {
    public ClinicianAlreadyExistsException(String message) {
        super(message);
    }
}
