package com.matibabu.backend.synchronization.outbox;

import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/*
 * A durable record of one committed clinical write, waiting to be
 * pushed to the central hub. Written by the same application service
 * that performs the domain mutation, in the same local transaction -
 * see docs/decisions/ADR-08-synchronization.md.
 *
 * Ordering falls out of `id` itself: like every other aggregate in
 * this codebase (ADR-004), it is a time-ordered UUID v7, so
 * `ORDER BY id` already reflects the order entries were recorded in.
 * No separate sequence number is needed.
 */
public class SyncOutboxEntry {

    private final UUID id;
    private final UUID facilityId;
    private final AggregateType aggregateType;
    private final UUID aggregateId;
    private final String operation;
    private final String payload;
    private final Instant createdAt;
    private final Instant syncedAt;

    private SyncOutboxEntry(
            UUID id,
            UUID facilityId,
            AggregateType aggregateType,
            UUID aggregateId,
            String operation,
            String payload,
            Instant createdAt,
            Instant syncedAt
    ) {
        this.id = Objects.requireNonNull(id, "Outbox entry ID cannot be null");
        this.facilityId = Objects.requireNonNull(facilityId, "An outbox entry must record which facility wrote it");
        this.aggregateType = Objects.requireNonNull(aggregateType, "An outbox entry must record its aggregate type");
        this.aggregateId = Objects.requireNonNull(aggregateId, "An outbox entry must record its aggregate ID");

        if (operation == null || operation.isBlank()) {
            throw new IllegalArgumentException("An outbox entry must record which operation produced it");
        }
        this.operation = operation;

        if (payload == null) {
            throw new IllegalArgumentException("An outbox entry must have a payload");
        }
        this.payload = payload;

        this.createdAt = Objects.requireNonNull(createdAt, "Created time cannot be null");
        this.syncedAt = syncedAt;
    }

    public static SyncOutboxEntry record(
            UUID facilityId,
            AggregateType aggregateType,
            UUID aggregateId,
            String operation,
            String payload,
            Instant now
    ) {
        return new SyncOutboxEntry(
                UuidCreator.getTimeOrderedEpoch(),
                facilityId,
                aggregateType,
                aggregateId,
                operation,
                payload,
                now,
                null
        );
    }

    public static SyncOutboxEntry reconstitute(
            UUID id,
            UUID facilityId,
            AggregateType aggregateType,
            UUID aggregateId,
            String operation,
            String payload,
            Instant createdAt,
            Instant syncedAt
    ) {
        return new SyncOutboxEntry(
                id,
                facilityId,
                aggregateType,
                aggregateId,
                operation,
                payload,
                createdAt,
                syncedAt
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getFacilityId() {
        return facilityId;
    }

    public AggregateType getAggregateType() {
        return aggregateType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getOperation() {
        return operation;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSyncedAt() {
        return syncedAt;
    }
}
