package com.matibabu.backend.api.encounter;

import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterStatus;

import java.time.Instant;
import java.util.UUID;

public record EncounterResponse(
        UUID id,
        UUID patientId,
        UUID attendingClinicianId,
        UUID facilityId,
        UUID departmentId,
        Instant startedAt,
        EncounterStatus status,
        Instant endedAt
) {

    public static EncounterResponse from(Encounter encounter) {
        return new EncounterResponse(
                encounter.getId(),
                encounter.getPatientId(),
                encounter.getAttendingClinicianId(),
                encounter.getFacilityId(),
                encounter.getDepartmentId(),
                encounter.getStartedAt(),
                encounter.getStatus(),
                encounter.getEndedAt()
        );
    }
}
