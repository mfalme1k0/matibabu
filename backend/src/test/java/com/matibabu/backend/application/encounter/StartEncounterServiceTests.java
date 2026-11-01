package com.matibabu.backend.application.encounter;

import com.matibabu.backend.config.NodeIdentity;
import com.matibabu.backend.domain.department.Department;
import com.matibabu.backend.domain.department.DepartmentRepository;
import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.domain.encounter.EncounterStatus;
import com.matibabu.backend.exception.DepartmentFacilityMismatchException;
import com.matibabu.backend.exception.DepartmentNotActiveException;
import com.matibabu.backend.exception.DepartmentNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartEncounterServiceTests {

    @Mock
    private EncounterRepository encounterRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private NodeIdentity nodeIdentity;

    private StartEncounterService service;

    @BeforeEach
    void setUp() {
        service = new StartEncounterService(
                encounterRepository,
                departmentRepository,
                nodeIdentity
        );
    }

    @Test
    void shouldStartAndSaveEncounter() {
        UUID patientId = UUID.randomUUID();
        UUID attendingClinicianId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        Instant startedAt = Instant.parse("2026-08-20T10:00:00Z");

        Department department = Department.reconstitute(
                departmentId,
                facilityId,
                "OPD",
                "Outpatient Department",
                true
        );

        when(nodeIdentity.facilityId()).thenReturn(facilityId);
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        when(encounterRepository.save(any(Encounter.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Encounter encounter = service.start(
                patientId,
                attendingClinicianId,
                departmentId,
                startedAt
        );

        assertNotNull(encounter.getId());
        assertEquals(patientId, encounter.getPatientId());
        assertEquals(attendingClinicianId, encounter.getAttendingClinicianId());
        assertEquals(facilityId, encounter.getFacilityId());
        assertEquals(departmentId, encounter.getDepartmentId());
        assertEquals(startedAt, encounter.getStartedAt());
        assertEquals(EncounterStatus.ACTIVE, encounter.getStatus());
        assertNull(encounter.getEndedAt());

        verify(departmentRepository).findById(departmentId);
        verify(encounterRepository).save(encounter);
    }

    @Test
    void shouldRejectDepartmentThatDoesNotExist() {
        UUID patientId = UUID.randomUUID();
        UUID attendingClinicianId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        Instant startedAt = Instant.parse("2026-08-20T10:00:00Z");

        when(nodeIdentity.facilityId()).thenReturn(facilityId);
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        DepartmentNotFoundException exception = assertThrows(
                DepartmentNotFoundException.class,
                () -> service.start(patientId, attendingClinicianId, departmentId, startedAt)
        );

        assertTrue(exception.getMessage().contains(departmentId.toString()));
        verify(departmentRepository).findById(departmentId);
        verify(encounterRepository, never()).save(any());
    }

    @Test
    void shouldRejectDepartmentFromAnotherFacility() {
        UUID patientId = UUID.randomUUID();
        UUID attendingClinicianId = UUID.randomUUID();
        UUID currentFacilityId = UUID.randomUUID();
        UUID departmentFacilityId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        Instant startedAt = Instant.parse("2026-08-20T10:00:00Z");

        Department department = Department.reconstitute(
                departmentId,
                departmentFacilityId,
                "OPD",
                "Outpatient Department",
                true
        );

        when(nodeIdentity.facilityId()).thenReturn(currentFacilityId);
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));

        DepartmentFacilityMismatchException exception = assertThrows(
                DepartmentFacilityMismatchException.class,
                () -> service.start(patientId, attendingClinicianId, departmentId, startedAt)
        );

        assertTrue(exception.getMessage().contains(departmentId.toString()));
        assertTrue(exception.getMessage().contains(currentFacilityId.toString()));
        verify(departmentRepository).findById(departmentId);
        verify(encounterRepository, never()).save(any());
    }

    @Test
    void shouldRejectInactiveDepartment() {
        UUID patientId = UUID.randomUUID();
        UUID attendingClinicianId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        Instant startedAt = Instant.parse("2026-08-20T10:00:00Z");

        Department department = Department.reconstitute(
                departmentId,
                facilityId,
                "OPD",
                "Outpatient Department",
                false
        );

        when(nodeIdentity.facilityId()).thenReturn(facilityId);
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));

        DepartmentNotActiveException exception = assertThrows(
                DepartmentNotActiveException.class,
                () -> service.start(patientId, attendingClinicianId, departmentId, startedAt)
        );

        assertTrue(exception.getMessage().contains(departmentId.toString()));
        verify(departmentRepository).findById(departmentId);
        verify(encounterRepository, never()).save(any());
    }
}
