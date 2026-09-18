package com.matibabu.backend.infrastructure.persistence.facility;

import com.matibabu.backend.domain.facility.Facility;
import com.matibabu.backend.domain.facility.FacilityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FacilityRepositoryAdapter implements FacilityRepository {

    private final SpringDataFacilitiesRepository jpaRepository;
    private final FacilityMapper mapper;

    public FacilityRepositoryAdapter(
            SpringDataFacilitiesRepository jpaRepository,
            FacilityMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Facility save(Facility facility) {
        FacilityEntity entity = mapper.toEntity(facility);
        FacilityEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Facility> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Facility> findByMflCode(String mflCode) {
        return jpaRepository.findByMflCode(mflCode)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByMflCode(String mflCode) {
        return jpaRepository.existsByMflCode(mflCode);
    }

    @Override
    public List<Facility> findAllActive() {
        return jpaRepository.findByActiveTrue()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Facility> search(String query) {
        return jpaRepository.findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndMflCodeContainingIgnoreCase(
                        query, query
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UUID facilityId) {
        return false;
    }
}
