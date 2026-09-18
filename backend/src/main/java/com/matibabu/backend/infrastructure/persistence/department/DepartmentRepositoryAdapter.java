package com.matibabu.backend.infrastructure.persistence.department;

import com.matibabu.backend.domain.department.Department;
import com.matibabu.backend.domain.department.DepartmentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DepartmentRepositoryAdapter
        implements DepartmentRepository {

    private final SpringDataDepartmentsRepository jpaRepository;
    private final DepartmentMapper mapper;

    public DepartmentRepositoryAdapter(
            SpringDataDepartmentsRepository jpaRepository,
            DepartmentMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Department save(Department department) {
        DepartmentEntity entity = mapper.toEntity(department);

        DepartmentEntity savedEntity =
                jpaRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Department> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Department> findByFacilityId(UUID facilityId) {
        return jpaRepository.findByFacilityId(facilityId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Department> findByFacilityIdAndCode(
            UUID facilityId,
            String code
    ) {
        return jpaRepository
                .findByFacilityIdAndCode(facilityId, code)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByFacilityIdAndCode(
            UUID facilityId,
            String code
    ) {
        return jpaRepository.existsByFacilityIdAndCode(
                facilityId,
                code
        );
    }

    @Override
    public List<Department> findAllActive(UUID facilityId) {
        return jpaRepository.findByFacilityIdAndActiveTrue(facilityId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}