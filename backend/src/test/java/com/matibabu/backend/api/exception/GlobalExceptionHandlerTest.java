package com.matibabu.backend.api.exception;

import com.matibabu.backend.domain.encounter.EncounterNotActiveException;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    static Stream<Arguments> exceptionsAndExpectedStatus() {
        UUID id = UUID.randomUUID();
        return Stream.of(
                Arguments.of(new PatientNotFoundException(id), HttpStatus.NOT_FOUND),
                Arguments.of(new EncounterNotFoundException(id), HttpStatus.NOT_FOUND),
                Arguments.of(new MedicalRecordNotFoundException(id), HttpStatus.NOT_FOUND),
                Arguments.of(new FacilityNotFoundException(id), HttpStatus.NOT_FOUND),
                Arguments.of(new MedicineNotFoundException(id), HttpStatus.NOT_FOUND),
                Arguments.of(new ReferralNotFoundException(id), HttpStatus.NOT_FOUND),
                Arguments.of(new DepartmentNotFoundException(id), HttpStatus.NOT_FOUND),
                Arguments.of(new UserNotFoundException("Clinician not found"), HttpStatus.NOT_FOUND),
                Arguments.of(new DuplicatePhoneNumberException("+254700000000"), HttpStatus.CONFLICT),
                Arguments.of(new DuplicateMflCodeException("12345"), HttpStatus.CONFLICT),
                Arguments.of(new DepartmentCodeAlreadyExistsException(id, "OPD"), HttpStatus.CONFLICT),
                Arguments.of(new ClinicianAlreadyExistsException("already exists"), HttpStatus.CONFLICT),
                Arguments.of(new ReferralNotPendingException("not pending"), HttpStatus.CONFLICT),
                Arguments.of(new EncounterNotActiveException("not active"), HttpStatus.CONFLICT),
                Arguments.of(new InvalidDiagnosisReferenceException(id, id), HttpStatus.BAD_REQUEST)
        );
    }

    @ParameterizedTest
    @MethodSource("exceptionsAndExpectedStatus")
    void mapsEachDomainExceptionToItsExpectedStatus(RuntimeException exception, HttpStatus expectedStatus) {
        ResponseEntity<?> response = handler.handleDomainException(exception);

        assertEquals(expectedStatus, response.getStatusCode());
        assertEquals(exception.getMessage(), ((java.util.Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void mapsDataIntegrityViolationToConflict() {
        ResponseEntity<?> response = handler.handleDataIntegrityViolation(new DataIntegrityViolationException("boom"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void preservesStatusAndReasonFromResponseStatusException() {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "no coffee");

        ResponseEntity<?> response = handler.handleResponseStatusException(exception);

        assertEquals(HttpStatus.I_AM_A_TEAPOT, response.getStatusCode());
        assertEquals("no coffee", ((java.util.Map<?, ?>) response.getBody()).get("error"));
    }
}
