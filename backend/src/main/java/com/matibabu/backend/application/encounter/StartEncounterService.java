package com.matibabu.backend.application.encounter;

import com.matibabu.backend.config.NodeIdentity;
import com.matibabu.backend.domain.department.Department;
import com.matibabu.backend.domain.department.DepartmentRepository;
import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.exception.DepartmentFacilityMismatchException;
import com.matibabu.backend.exception.DepartmentNotActiveException;
import com.matibabu.backend.exception.DepartmentNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class StartEncounterService implements StartEncounterUseCase {

    private final EncounterRepository encounterRepository;
    private final DepartmentRepository departmentRepository;
    private final NodeIdentity nodeIdentity;

    public StartEncounterService(
            EncounterRepository encounterRepository,
            DepartmentRepository departmentRepository,
            NodeIdentity nodeIdentity
    ) {
        this.encounterRepository = encounterRepository;
        this.departmentRepository = departmentRepository;
        this.nodeIdentity = nodeIdentity;
    }

    @Override
    public Encounter start(
            UUID patientId,
            UUID attendingClinicianId,
            UUID departmentId,
            Instant now
    ) {
        UUID facilityId = nodeIdentity.facilityId();

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));

        if (!department.getFacilityId().equals(facilityId)) {
            throw new DepartmentFacilityMismatchException(departmentId, facilityId);
        }

        if (!department.isActive()) {
            throw new DepartmentNotActiveException(departmentId);
        }

        Encounter encounter = Encounter.start(
                patientId,
                attendingClinicianId,
                facilityId,
                departmentId,
                now
        );

        return encounterRepository.save(encounter);
    }
}
