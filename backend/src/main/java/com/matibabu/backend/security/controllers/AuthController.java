package com.matibabu.backend.security.controllers;

import com.matibabu.backend.security.entity.Clinician;
import com.matibabu.backend.security.services.ClinicianService;
import com.matibabu.backend.security.dto.RegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ClinicianService clinicianService;

    public AuthController(ClinicianService clinicianService) {
        this.clinicianService = clinicianService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegistrationRequest registrationRequest) {

        Clinician clinician = clinicianService.registerClinician(registrationRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        Map.of(
                                "message", "Clinician registered successfully",
                                "email", clinician.getEmail(),
                                "role", clinician.getRole().name()
                        )
                );
    }
}
