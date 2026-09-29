package com.matibabu.backend.application.patient;

import com.matibabu.backend.domain.patient.Gender;
import com.matibabu.backend.domain.patient.Patient;
import com.matibabu.backend.domain.patient.PatientRepository;
import com.matibabu.backend.exception.DuplicatePhoneNumberException;
import com.matibabu.backend.exception.PatientNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
@Service

public class UpdatePatientService implements UpdatePatientUseCase {

    private final PatientRepository patientRepository;

    public UpdatePatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public Patient update(
            UUID id,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            Gender gender,
            String phoneNumber,
            String address,
            String nationalId,
            String birthCertificateNumber
    ) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        Optional<Patient> existingWithPhone = patientRepository.findByPhoneNumber(phoneNumber);
        if (existingWithPhone.isPresent() && !existingWithPhone.get().getId().equals(id)) {
            throw new DuplicatePhoneNumberException(phoneNumber);
        }

        patient.update(
                firstName,
                lastName,
                dateOfBirth,
                gender,
                phoneNumber,
                address,
                nationalId,
                birthCertificateNumber
        );

        return patientRepository.save(patient);
    }
}
