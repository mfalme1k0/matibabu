package com.matibabu.backend.application.patient;

import com.matibabu.backend.domain.patient.Patient;
import com.matibabu.backend.domain.patient.PatientRepository;
import com.matibabu.backend.exception.PatientNotFoundException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
@Service


public class SearchPatientByPhoneNumberService implements SearchPatientByPhoneNumberUseCase {

    private final PatientRepository patientRepository;

    public SearchPatientByPhoneNumberService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public Patient searchByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new PatientNotFoundException("Patient not found with empty phone number");
        }

        Set<String> candidates = generateSearchCandidates(phoneNumber);
        for (String candidate : candidates) {
            Optional<Patient> patient = patientRepository.findByPhoneNumber(candidate);
            if (patient.isPresent()) {
                return patient.get();
            }
        }

        throw new PatientNotFoundException("Patient not found with phone number: " + phoneNumber);
    }

    private Set<String> generateSearchCandidates(String raw) {
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(raw);
        candidates.add(raw.trim());

        // Handle URL-decoded '+' that turns into a leading space (e.g., " 254712345678")
        String trimmed = raw.trim();
        if (raw.startsWith(" ") && !trimmed.startsWith("+")) {
            candidates.add("+" + trimmed);
        }

        // Clean digits and '+'
        String cleaned = raw.replaceAll("[^0-9+]", "");
        if (!cleaned.isEmpty()) {
            candidates.add(cleaned);
            if (!cleaned.startsWith("+")) {
                candidates.add("+" + cleaned);
            } else {
                candidates.add(cleaned.substring(1));
            }
        }

        // Handle Kenyan local (07...) vs international (+2547... / 2547...) formats
        if (cleaned.startsWith("0") && cleaned.length() >= 9) {
            candidates.add("+254" + cleaned.substring(1));
            candidates.add("254" + cleaned.substring(1));
        } else if (cleaned.startsWith("+254") && cleaned.length() >= 12) {
            candidates.add("0" + cleaned.substring(4));
            candidates.add(cleaned.substring(1));
        } else if (cleaned.startsWith("254") && cleaned.length() >= 11) {
            candidates.add("0" + cleaned.substring(3));
            candidates.add("+" + cleaned);
        }

        return candidates;
    }
}
