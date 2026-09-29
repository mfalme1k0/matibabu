package com.matibabu.backend.application.encounter;

import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.exception.EncounterNotActiveException;
import com.matibabu.backend.domain.encounter.EncounterStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EncounterTest {


    @Test
    void shouldStartAsActive() {
        UUID patientId = UUID.randomUUID();
        UUID attendingClinicianId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        Instant now = Instant.now();

        // Start a new encounter for the patient.
        Encounter encounter =
                Encounter.start(
                        patientId,
                        attendingClinicianId,
                        facilityId,
                        departmentId,
                        now
                );

        // The domain should generate an ID for the new encounter.
        assertNotNull(encounter.getId());

        // The supplied patient ID should be preserved.
        assertEquals(patientId, encounter.getPatientId());

        // The attending clinician should be preserved.
        assertEquals(
                attendingClinicianId,
                encounter.getAttendingClinicianId()
        );

        // The supplied facility should be preserved.
        assertEquals(facilityId, encounter.getFacilityId());

        // The supplied department should be preserved.
        assertEquals(departmentId, encounter.getDepartmentId());

        // The supplied start time should be preserved.
        assertEquals(now, encounter.getStartedAt());

        // A newly started encounter must always be ACTIVE.
        assertEquals(
                EncounterStatus.ACTIVE,
                encounter.getStatus()
        );

        // An active encounter has not ended yet.
        assertNull(encounter.getEndedAt());
    }


    @Test
    void shouldDischargeActiveEncounter() {
        UUID patientId = UUID.randomUUID();
        UUID attendingClinicianId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        Instant startedAt =
                Instant.parse("2026-08-19T10:00:00Z");

        Instant dischargedAt =
                Instant.parse("2026-08-19T12:00:00Z");

        // Create an active encounter.
        Encounter encounter =
                Encounter.start(
                        patientId,
                        attendingClinicianId,
                        facilityId,
                        departmentId,
                        startedAt
                );

        // Perform the domain operation.
        encounter.discharge(dischargedAt);

        // The encounter should now be DISCHARGED.
        assertEquals(
                EncounterStatus.DISCHARGED,
                encounter.getStatus()
        );

        // The discharge time should be stored as the end time.
        assertEquals(
                dischargedAt,
                encounter.getEndedAt()
        );
    }



    @Test
    void shouldCancelActiveEncounter() {
        UUID patientId = UUID.randomUUID();
        UUID attendingClinicianId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        Instant startedAt =
                Instant.parse("2026-08-19T10:00:00Z");

        Instant cancelledAt =
                Instant.parse("2026-08-19T10:30:00Z");

        // Create an active encounter.
        Encounter encounter =
                Encounter.start(
                        patientId,
                        attendingClinicianId,
                        facilityId,
                        departmentId,
                        startedAt
                );

        // Cancel the encounter.
        encounter.cancel(cancelledAt);

        // Verify that the status changed correctly.
        assertEquals(
                EncounterStatus.CANCELLED,
                encounter.getStatus()
        );

        // Cancellation also records when the encounter ended.
        assertEquals(
                cancelledAt,
                encounter.getEndedAt()
        );
    }


    @Test
    void shouldNotDischargeAlreadyDischargedEncounter() {
        UUID patientId = UUID.randomUUID();
        UUID attendingClinicianId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        Instant startedAt =
                Instant.parse("2026-08-19T10:00:00Z");

        Instant dischargedAt =
                Instant.parse("2026-08-19T12:00:00Z");

        // Start the encounter.
        Encounter encounter =
                Encounter.start(
                        patientId,
                        attendingClinicianId,
                        facilityId,
                        departmentId,
                        startedAt
                );

        // Discharge it once.
        encounter.discharge(dischargedAt);

        // Attempting to discharge it again should fail.
        assertThrows(
                EncounterNotActiveException.class,
                () -> encounter.discharge(dischargedAt)
        );
    }


    @Test
    void shouldNotCancelDischargedEncounter() {
        UUID patientId = UUID.randomUUID();
        UUID attendingClinicianId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        Instant startedAt =
                Instant.parse("2026-08-19T10:00:00Z");

        Instant dischargedAt =
                Instant.parse("2026-08-19T12:00:00Z");

        // Create the encounter.
        Encounter encounter =
                Encounter.start(
                        patientId,
                        attendingClinicianId,
                        facilityId,
                        departmentId,
                        startedAt
                );

        // Move the encounter into its terminal DISCHARGED state.
        encounter.discharge(dischargedAt);

        // Cancellation after discharge must be rejected.
        assertThrows(
                EncounterNotActiveException.class,
                () -> encounter.cancel(dischargedAt)
        );
    }



    @Test
    void shouldNotAllowEndTimeBeforeStartTime() {
        UUID patientId = UUID.randomUUID();
        UUID attendingClinicianId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        Instant startedAt =
                Instant.parse("2026-08-19T12:00:00Z");

        Instant invalidEndTime =
                Instant.parse("2026-08-19T10:00:00Z");

        // Create the encounter.
        Encounter encounter =
                Encounter.start(
                        patientId,
                        attendingClinicianId,
                        facilityId,
                        departmentId,
                        startedAt
                );

        // Attempt to discharge using a time before the encounter started.
        assertThrows(
                IllegalArgumentException.class,
                () -> encounter.discharge(invalidEndTime)
        );
    }



    @Test
    void shouldNotStartEncounterWithoutFacility() {
        UUID patientId = UUID.randomUUID();
        UUID attendingClinicianId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();

        Instant now = Instant.now();

        assertThrows(
                NullPointerException.class,
                () -> Encounter.start(
                        patientId,
                        attendingClinicianId,
                        null,
                        departmentId,
                        now
                )
        );
    }



    @Test
    void shouldReconstituteEncounterWithoutFacilityOrDepartment() {
        UUID id = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID attendingClinicianId = UUID.randomUUID();

        Instant startedAt =
                Instant.parse("2026-08-19T10:00:00Z");

        Instant endedAt =
                Instant.parse("2026-08-19T12:00:00Z");

        Encounter encounter =
                Encounter.reconstitute(
                        id,
                        patientId,
                        attendingClinicianId,
                        null,
                        null,
                        startedAt,
                        EncounterStatus.DISCHARGED,
                        endedAt
                );

        assertEquals(id, encounter.getId());
        assertEquals(patientId, encounter.getPatientId());
        assertEquals(
                attendingClinicianId,
                encounter.getAttendingClinicianId()
        );

        assertNull(encounter.getFacilityId());
        assertNull(encounter.getDepartmentId());

        assertEquals(startedAt, encounter.getStartedAt());
        assertEquals(
                EncounterStatus.DISCHARGED,
                encounter.getStatus()
        );
        assertEquals(endedAt, encounter.getEndedAt());
    }
}

