package com.matibabu.backend.application.patient;

import com.matibabu.backend.domain.patient.Patient;
import com.matibabu.backend.domain.patient.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service

public class ListPatientsService implements ListPatientsUseCase {

    private final PatientRepository patientRepository;

    public ListPatientsService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public Page<Patient> list(Pageable pageable) {
        return patientRepository.findAll(pageable);
    }
}
