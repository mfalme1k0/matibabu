package com.matibabu.backend.infrastructure.persistence.encounter;

import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class EncounterRepositoryAdapter implements EncounterRepository {

    private final SpringDataEncounterRepository jpaRepository;
    private final EncounterMapper mapper;

    public EncounterRepositoryAdapter(
            SpringDataEncounterRepository jpaRepository,
            EncounterMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Encounter save(Encounter encounter) {
        EncounterEntity entity = mapper.toEntity(encounter);

        EncounterEntity savedEntity = jpaRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Encounter> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
}