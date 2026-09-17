package com.matibabu.backend.synchronization.outbox;

import com.matibabu.backend.config.NodeIdentity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/*
 * The one thing application services call to participate in sync.
 * facilityId always comes from this node's own configured identity,
 * never from the aggregate being written - most aggregates (Patient,
 * MedicalRecord) don't carry a facilityId of their own at all, and
 * the ones that do (Encounter.facilityId, Referral.receivingFacilityId)
 * mean something different from "which facility recorded this write".
 */
@Component
public class SyncOutboxRecorder {

    private final SyncOutboxRepository outboxRepository;
    private final NodeIdentity nodeIdentity;
    private final ObjectMapper objectMapper;

    public SyncOutboxRecorder(
            SyncOutboxRepository outboxRepository,
            NodeIdentity nodeIdentity,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = outboxRepository;
        this.nodeIdentity = nodeIdentity;
        this.objectMapper = objectMapper;
    }

    public void record(
            AggregateType aggregateType,
            UUID aggregateId,
            String operation,
            Map<String, Object> payload
    ) {
        String json = objectMapper.writeValueAsString(payload);

        SyncOutboxEntry entry = SyncOutboxEntry.record(
                nodeIdentity.facilityId(),
                aggregateType,
                aggregateId,
                operation,
                json,
                Instant.now()
        );

        outboxRepository.save(entry);
    }
}
