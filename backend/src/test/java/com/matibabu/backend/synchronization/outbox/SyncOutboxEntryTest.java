package com.matibabu.backend.synchronization.outbox;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SyncOutboxEntryTest {

    @Test
    void recordMintsIdAndLeavesSyncedAtNull() {
        UUID facilityId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        Instant now = Instant.parse("2026-09-17T08:00:00Z");

        SyncOutboxEntry entry = SyncOutboxEntry.record(
                facilityId,
                AggregateType.PATIENT,
                aggregateId,
                "PatientRegistered",
                "{\"firstName\":\"John\"}",
                now
        );

        assertNotNull(entry.getId());
        assertEquals(7, entry.getId().version());
        assertEquals(facilityId, entry.getFacilityId());
        assertEquals(AggregateType.PATIENT, entry.getAggregateType());
        assertEquals(aggregateId, entry.getAggregateId());
        assertEquals("PatientRegistered", entry.getOperation());
        assertEquals("{\"firstName\":\"John\"}", entry.getPayload());
        assertEquals(now, entry.getCreatedAt());
        assertNull(entry.getSyncedAt());
    }

    @Test
    void reconstituteRoundTripsAllFields() {
        UUID id = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-09-17T08:00:00Z");
        Instant syncedAt = Instant.parse("2026-09-17T09:00:00Z");

        SyncOutboxEntry entry = SyncOutboxEntry.reconstitute(
                id,
                facilityId,
                AggregateType.REFERRAL,
                aggregateId,
                "ReferralCompleted",
                "{\"resolvedBy\":\"...\"}",
                createdAt,
                syncedAt
        );

        assertEquals(id, entry.getId());
        assertEquals(facilityId, entry.getFacilityId());
        assertEquals(AggregateType.REFERRAL, entry.getAggregateType());
        assertEquals(aggregateId, entry.getAggregateId());
        assertEquals("ReferralCompleted", entry.getOperation());
        assertEquals("{\"resolvedBy\":\"...\"}", entry.getPayload());
        assertEquals(createdAt, entry.getCreatedAt());
        assertEquals(syncedAt, entry.getSyncedAt());
    }

    @Test
    void requiresNonBlankOperation() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SyncOutboxEntry.record(
                        UUID.randomUUID(),
                        AggregateType.ENCOUNTER,
                        UUID.randomUUID(),
                        "  ",
                        "{}",
                        Instant.now()
                )
        );
    }
}
