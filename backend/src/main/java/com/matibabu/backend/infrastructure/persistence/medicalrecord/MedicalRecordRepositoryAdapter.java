package com.matibabu.backend.infrastructure.persistence.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MedicalRecordRepositoryAdapter implements MedicalRecordRepository {

    private final SpringDataMedicalRecordsRepository jpaRepository;
    private final SpringDataTreatmentsRepository treatmentsRepository;
    private final MedicalRecordMapper mapper;
    private final TreatmentMapper treatmentMapper;
    private final SpringDataAllergiesRepository allergiesRepository;
    private final AllergyMapper allergyMapper;

    public MedicalRecordRepositoryAdapter(
        SpringDataMedicalRecordsRepository jpaRepository,
        SpringDataTreatmentsRepository treatmentsRepository,
        MedicalRecordMapper mapper,
        TreatmentMapper treatmentMapper,
        SpringDataAllergiesRepository allergiesRepository,
        AllergyMapper allergyMapper
    ) {
        this.jpaRepository = jpaRepository;
        this.treatmentsRepository = treatmentsRepository;
        this.mapper = mapper;
        this.treatmentMapper = treatmentMapper;
        this.allergiesRepository = allergiesRepository;
        this.allergyMapper = allergyMapper;
    }

    @Override
    @Transactional
    public MedicalRecord save(MedicalRecord medicalRecord) {
        MedicalRecordEntity entity = mapper.toEntity(medicalRecord);
        MedicalRecordEntity savedEntity = jpaRepository.save(entity);

        // Treatments are re-synced in full on every save. This keeps the
        // aggregate's persistence simple and correct at the current
        // volume (a handful of treatments per record); if that becomes
        // a bottleneck, switch to diffing/updating individual rows.
        treatmentsRepository.deleteByMedicalRecordId(medicalRecord.getId());
        List<TreatmentEntity> treatmentEntities = medicalRecord.getTreatments().stream()
            .map(treatmentMapper::toEntity)
            .toList();
        treatmentsRepository.saveAll(treatmentEntities);

        allergiesRepository.deleteByMedicalRecordId(medicalRecord.getId());

        List<AllergyEntity> allergyEntities = medicalRecord.getAllergies().stream()
            .map(allergyMapper::toEntity)
            .toList();

        allergiesRepository.saveAll(allergyEntities); 

        return hydrate(savedEntity);  
    }

    @Override
    public Optional<MedicalRecord> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::hydrate);
    }

    @Override
    public List<MedicalRecord> findByPatientId(UUID patientId) {
        return jpaRepository.findByPatientId(patientId).stream()
                .map(this::hydrate)
                .toList();
    }

    @Override
    public Optional<MedicalRecord> findByEncounterId(UUID encounterId) {
        return jpaRepository.findByEncounterId(encounterId)
                .map(this::hydrate);
    }

    /*
     * Reconstructs the full MedicalRecord aggregate, including its
     * treatments, from persisted data.
     *
     * Note: vitals, observations, and diagnoses have the same
     * one-to-many shape as treatments but are not yet re-hydrated
     * here — see docs/decisions for the follow-up tracking this.
     */
    private MedicalRecord hydrate(MedicalRecordEntity entity) {
        MedicalRecord medicalRecord = mapper.toDomain(entity);
         
        //Load treatments
        treatmentsRepository.findByMedicalRecordId(entity.getId())
            .forEach(treatmentEntity ->
                 medicalRecord.addTreatment(treatmentMapper.toDomain(treatmentEntity))
            );

        
        //Load allergies
        allergiesRepository.findByMedicalRecordId(entity.getId())
            .forEach(allergyEntity ->
                medicalRecord.addAllergy(allergyMapper.toDomain(allergyEntity))
            );

         return medicalRecord;

    }
}
