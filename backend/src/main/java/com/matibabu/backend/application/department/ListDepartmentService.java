package com.matibabu.backend.application.department;

import com.matibabu.backend.domain.department.Department;
import com.matibabu.backend.domain.department.DepartmentRepository;
import com.matibabu.backend.domain.facility.FacilityRepository;
import com.matibabu.backend.exception.DepartmentNotFoundException;
import com.matibabu.backend.exception.FacilityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListDepartmentService implements ListDepartmentUseCase {

    private final DepartmentRepository departmentRepository;
    private final FacilityRepository facilityRepository;

    public ListDepartmentService(
            DepartmentRepository departmentRepository,
            FacilityRepository facilityRepository
    ) {
        this.departmentRepository = departmentRepository;
        this.facilityRepository = facilityRepository;
    }

    @Override
    public Department getById(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));
    }

    @Override
    public List<Department> findByFacilityId(UUID facilityId) {
        ensureFacilityExists(facilityId);

        return departmentRepository.findByFacilityId(facilityId);
    }

    @Override
    public List<Department> findAllActive(UUID facilityId) {
        ensureFacilityExists(facilityId);

        return departmentRepository.findAllActive(facilityId);
    }

    private void ensureFacilityExists(UUID facilityId) {
        facilityRepository.findById(facilityId)
                .orElseThrow(() -> new FacilityNotFoundException(facilityId));
    }
}

