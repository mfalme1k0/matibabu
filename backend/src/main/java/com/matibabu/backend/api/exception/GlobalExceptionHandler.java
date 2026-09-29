package com.matibabu.backend.api.exception;

import com.matibabu.backend.exception.EncounterNotActiveException;
import com.matibabu.backend.exception.ClinicianAlreadyExistsException;
import com.matibabu.backend.exception.DepartmentCodeAlreadyExistsException;
import com.matibabu.backend.exception.DepartmentNotFoundException;
import com.matibabu.backend.exception.DuplicateMflCodeException;
import com.matibabu.backend.exception.DuplicatePhoneNumberException;
import com.matibabu.backend.exception.EncounterNotFoundException;
import com.matibabu.backend.exception.FacilityNotFoundException;
import com.matibabu.backend.exception.InvalidDiagnosisReferenceException;
import com.matibabu.backend.exception.MedicalRecordNotFoundException;
import com.matibabu.backend.exception.MedicineNotFoundException;
import com.matibabu.backend.exception.PatientNotFoundException;
import com.matibabu.backend.exception.ReferralNotFoundException;
import com.matibabu.backend.exception.ReferralNotPendingException;
import com.matibabu.backend.exception.UserNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<Class<? extends RuntimeException>, HttpStatus> STATUS_BY_EXCEPTION = Map.ofEntries(
            // Not found
            Map.entry(PatientNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(EncounterNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(MedicalRecordNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(FacilityNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(MedicineNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(ReferralNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(DepartmentNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(UserNotFoundException.class, HttpStatus.NOT_FOUND),

            // Conflict: the request is well-formed but clashes with existing state
            Map.entry(DuplicatePhoneNumberException.class, HttpStatus.CONFLICT),
            Map.entry(DuplicateMflCodeException.class, HttpStatus.CONFLICT),
            Map.entry(DepartmentCodeAlreadyExistsException.class, HttpStatus.CONFLICT),
            Map.entry(ClinicianAlreadyExistsException.class, HttpStatus.CONFLICT),
            Map.entry(ReferralNotPendingException.class, HttpStatus.CONFLICT),
            Map.entry(EncounterNotActiveException.class, HttpStatus.CONFLICT),

            // Bad request: the request refers to something invalid/inconsistent
            Map.entry(InvalidDiagnosisReferenceException.class, HttpStatus.BAD_REQUEST)
    );

    @ExceptionHandler({
            PatientNotFoundException.class,
            EncounterNotFoundException.class,
            MedicalRecordNotFoundException.class,
            FacilityNotFoundException.class,
            MedicineNotFoundException.class,
            ReferralNotFoundException.class,
            DepartmentNotFoundException.class,
            UserNotFoundException.class,
            DuplicatePhoneNumberException.class,
            DuplicateMflCodeException.class,
            DepartmentCodeAlreadyExistsException.class,
            ClinicianAlreadyExistsException.class,
            ReferralNotPendingException.class,
            EncounterNotActiveException.class,
            InvalidDiagnosisReferenceException.class
    })
    public ResponseEntity<Map<String, String>> handleDomainException(RuntimeException exception) {
        HttpStatus status = STATUS_BY_EXCEPTION.getOrDefault(exception.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(status).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Data integrity conflict: unique constraint violated"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(
            ResponseStatusException exception
    ) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(Map.of("error", exception.getReason() != null ? exception.getReason() : exception.getMessage()));
    }
}
