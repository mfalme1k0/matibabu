package com.matibabu.backend.application.department;

import com.matibabu.backend.domain.department.Department;
import com.matibabu.backend.domain.department.DepartmentRepository;
import com.matibabu.backend.domain.facility.FacilityRepository;
import com.matibabu.backend.exception.DepartmentCodeAlreadyExistsException;
import com.matibabu.backend.exception.FacilityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateDepartmentService implements CreateDepartmentUseCase{
  private final DepartmentRepository departmentRepository ;
  private final FacilityRepository facilityRepository;

    public CreateDepartmentService (DepartmentRepository departmentRepository, FacilityRepository facilityRepository){
     this.departmentRepository = departmentRepository;
     this.facilityRepository = facilityRepository;
 }

    @Override
    public Department Create(UUID facilityId, String code, String name) {
        if(!facilityRepository.existsById(facilityId)){
            throw new FacilityNotFoundException(facilityId);
        }
        if(departmentRepository.existsByFacilityIdAndCode(facilityId, code)){
            throw new DepartmentCodeAlreadyExistsException(facilityId, code);
        }

        Department department = Department.create(facilityId, code, name);

        return departmentRepository.save(department);
    }
}
