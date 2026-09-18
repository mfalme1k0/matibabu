package com.matibabu.backend.application.department;

import com.matibabu.backend.domain.department.Department;
import com.matibabu.backend.domain.department.DepartmentRepository;
import com.matibabu.backend.exception.DepartmentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeactivateDepartmentService implements  DeactivateDepartmentUseCase{
    private final DepartmentRepository departmentRepository;

    public DeactivateDepartmentService(DepartmentRepository departmentRepository){
        this.departmentRepository = departmentRepository;

    }

    @Override
    @Transactional
    public Department deactivate(UUID Id){
        Department department = departmentRepository.findById(Id)
                .orElseThrow(() -> new DepartmentNotFoundException(Id));

        department.deactivate();
        return departmentRepository.save(department);
    }
}
