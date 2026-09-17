package com.matibabu.backend.application.patient;

import com.matibabu.backend.domain.patient.Gender;
import com.matibabu.backend.domain.patient.Patient;
import com.matibabu.backend.domain.patient.PatientRepository;
import com.matibabu.backend.exception.DuplicatePhoneNumberException;
import com.matibabu.backend.exception.PatientNotFoundException;
import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UpdatePatientService implements UpdatePatientUseCase {

    private final PatientRepository patientRepository;
    private final SyncOutboxRecorder syncOutboxRecorder;

    public UpdatePatientService(PatientRepository patientRepository, SyncOutboxRecorder syncOutboxRecorder) {
        this.patientRepository = patientRepository;
        this.syncOutboxRecorder = syncOutboxRecorder;
    }

    @Override
    @Transactional
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

        syncOutboxRecorder.record(AggregateType.PATIENT, saved.getId(), "PatientUpdated", payload);

        return saved;
    }
}
