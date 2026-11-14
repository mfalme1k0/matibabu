package com.matibabu.backend.infrastructure.persistence.medicalrecord;

import com.matibabu.backend.domain.medicalrecord.Allergy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AllergyMapper {

    default AllergyEntity toEntity(Allergy allergy) {
        if (allergy == null) {
            return null;
        }

        return new AllergyEntity(
            allergy.getId(),
            allergy.getMedicalRecordId(),
            allergy.getAllergen(),
            allergy.getReaction(),
            allergy.getRecordedAt()
        );
    }

    default Allergy toDomain(AllergyEntity entity) {
        if (entity == null) {
            return null;
        }

        return Allergy.reconstitute(
            entity.getId(),
            entity.getMedicalRecordId(),
            entity.getAllergen(),
            entity.getReaction(),
            entity.getRecordedAt()
        );
    }
}