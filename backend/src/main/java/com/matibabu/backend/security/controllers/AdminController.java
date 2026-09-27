package com.matibabu.backend.security.controllers;

import com.matibabu.backend.security.entity.Clinician;
import com.matibabu.backend.security.services.ClinicianService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ClinicianService clinicianService;

    public AdminController(ClinicianService clinicianService) {
        this.clinicianService = clinicianService;
    }

    @PostMapping("/clinicians/{id}/promote")
    public ResponseEntity<?> promoteToAdmin(@PathVariable UUID id) {
        Clinician clinician = clinicianService.promoteToAdmin(id);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Clinician promoted to ADMIN",
                        "id", clinician.getId(),
                        "email", clinician.getEmail(),
                        "role", clinician.getRole().name()
                )
        );
    }
}
