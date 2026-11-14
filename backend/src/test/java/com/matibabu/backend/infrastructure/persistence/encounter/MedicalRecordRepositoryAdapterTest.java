package com.matibabu.backend.infrastructure.persistence.encounter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;


import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.Allergy;
import com.matibabu.backend.infrastructure.persistence.medicalrecord.MedicalRecordRepositoryAdapter;
import com.matibabu.backend.infrastructure.persistence.medicalrecord.MedicalRecordMapperImpl;
import com.matibabu.backend.infrastructure.persistence.medicalrecord.TreatmentMapperImpl;
import com.matibabu.backend.infrastructure.persistence.medicalrecord.AllergyMapperImpl;

import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    MedicalRecordRepositoryAdapter.class,
    MedicalRecordMapperImpl.class,
    TreatmentMapperImpl.class,
    AllergyMapperImpl.class
})
class MedicalRecordRepositoryAdapterTest {
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    

    
    @Test
    void shouldSaveAndRetrieveMedicalRecordWithAllergy() {
        // Arrange
        UUID patientId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();

        MedicalRecord medicalRecord =
            new MedicalRecord(patientId, encounterId);

        Allergy allergy = new Allergy(
            medicalRecord.getId(),
            "Penicillin",
            "Skin rash"
        );

        medicalRecord.addAllergy(allergy);

        // Act: Save the medical record.
        MedicalRecord saved =
            medicalRecordRepository.save(medicalRecord);

        // Act: Retrieve it again from the repository.
        MedicalRecord retrieved =
            medicalRecordRepository.findById(saved.getId())
            .orElseThrow();

        // Assert
        assertEquals(saved.getId(), retrieved.getId());
        assertEquals(1, retrieved.getAllergies().size());

        Allergy retrievedAllergy = retrieved.getAllergies().get(0);

        assertEquals(allergy.getId(), retrievedAllergy.getId());
        assertEquals(saved.getId(), retrievedAllergy.getMedicalRecordId());
        assertEquals("Penicillin", retrievedAllergy.getAllergen());
        assertEquals("Skin rash", retrievedAllergy.getReaction());
        assertEquals(allergy.getRecordedAt(), retrievedAllergy.getRecordedAt());
    }

}

// vitals,observations and diagnoses are not yet rehydrated