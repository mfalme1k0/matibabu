package com.matibabu.backend.application.patient;

import com.matibabu.backend.domain.patient.Patient;
import com.matibabu.backend.domain.patient.PatientRepository;
import com.matibabu.backend.exception.PatientNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service

public class GetPatientService implements GetPatientUseCase {

    private final PatientRepository patientRepository;

    public GetPatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public Patient getById(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }
}