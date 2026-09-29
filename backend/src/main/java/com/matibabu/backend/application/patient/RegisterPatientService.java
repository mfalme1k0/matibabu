package com.matibabu.backend.application.patient;

import com.matibabu.backend.domain.patient.Gender;
import com.matibabu.backend.domain.patient.Patient;
import com.matibabu.backend.domain.patient.PatientRepository;
import com.matibabu.backend.exception.DuplicatePhoneNumberException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
@Service

public class RegisterPatientService implements RegisterPatientUseCase {

    private final PatientRepository patientRepository;

    public RegisterPatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public Patient register(
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            Gender gender,
            String phoneNumber,
            String address,
            String nationalId,
            String birthCertificateNumber
    ) {
        if (patientRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicatePhoneNumberException(phoneNumber);
        }

        Patient patient = new Patient(
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