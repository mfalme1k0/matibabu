package com.matibabu.backend.infrastructure.persistence.outbox;

import com.matibabu.backend.synchronization.outbox.SyncOutboxEntry;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SyncOutboxMapper {

    SyncOutboxEntity toEntity(SyncOutboxEntry entry);

    default SyncOutboxEntry toDomain(SyncOutboxEntity entity) {
        if (entity == null) {
            return null;
        }

        return SyncOutboxEntry.reconstitute(
                entity.getId(),
                entity.getFacilityId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getOperation(),
                entity.getPayload(),
                entity.getCreatedAt(),
                entity.getSyncedAt()
        );
    }
}
