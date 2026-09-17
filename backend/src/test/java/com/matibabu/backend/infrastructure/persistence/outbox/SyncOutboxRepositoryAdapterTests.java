package com.matibabu.backend.infrastructure.persistence.outbox;

import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxEntry;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        SyncOutboxRepositoryAdapter.class,
        SyncOutboxMapperImpl.class
})
class SyncOutboxRepositoryAdapterTests {

    @Autowired
    private SyncOutboxRepository outboxRepository;

    @Test
    void shouldSaveAndFindEntry() {
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

        SyncOutboxEntry saved = outboxRepository.save(entry);

        assertNotNull(saved);
        assertEquals(entry.getId(), saved.getId());

        SyncOutboxEntry retrieved = outboxRepository.findById(entry.getId())
                .orElseThrow();

        assertEquals(entry.getId(), retrieved.getId());
        assertEquals(facilityId, retrieved.getFacilityId());
        assertEquals(AggregateType.PATIENT, retrieved.getAggregateType());
        assertEquals(aggregateId, retrieved.getAggregateId());
        assertEquals("PatientRegistered", retrieved.getOperation());
        assertEquals("{\"firstName\":\"John\"}", retrieved.getPayload());
        assertEquals(now, retrieved.getCreatedAt());
        assertNull(retrieved.getSyncedAt());
    }
}
