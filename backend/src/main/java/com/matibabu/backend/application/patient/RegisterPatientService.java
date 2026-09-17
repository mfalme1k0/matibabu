package com.matibabu.backend.application.patient;

import com.matibabu.backend.domain.patient.Gender;
import com.matibabu.backend.domain.patient.Patient;
import com.matibabu.backend.domain.patient.PatientRepository;
import com.matibabu.backend.exception.DuplicatePhoneNumberException;
import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class RegisterPatientService implements RegisterPatientUseCase {

    private final PatientRepository patientRepository;
    private final SyncOutboxRecorder syncOutboxRecorder;

    public RegisterPatientService(PatientRepository patientRepository, SyncOutboxRecorder syncOutboxRecorder) {
        this.patientRepository = patientRepository;
        this.syncOutboxRecorder = syncOutboxRecorder;
    }

    @Override
    @Transactional
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

        Patient saved = patientRepository.save(patient);

        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", firstName);
        payload.put("lastName", lastName);
        payload.put("dateOfBirth", dateOfBirth.toString());
        payload.put("gender", gender.name());
        payload.put("phoneNumber", phoneNumber);
        payload.put("address", address);
        payload.put("nationalId", nationalId);
        payload.put("birthCertificateNumber", birthCertificateNumber);

        syncOutboxRecorder.record(AggregateType.PATIENT, saved.getId(), "PatientRegistered", payload);

        return saved;
    }
}