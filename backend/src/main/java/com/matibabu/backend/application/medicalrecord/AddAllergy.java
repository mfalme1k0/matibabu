package com.matibabu.backend.application.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.Allergy;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AddAllergy {

    private final MedicalRecordRepository medicalRecordRepository;

    public AddAllergy(MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
    }

    public MedicalRecord execute(
        UUID medicalRecordId,
        String allergen,
        String reaction
    ) {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId).orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + medicalRecordId));


        Allergy allergy = new Allergy(
            medicalRecordId,
            allergen,
            reaction
        );

        medicalRecord.addAllergy(allergy);

        return medicalRecordRepository.save(medicalRecord);
    }
}
