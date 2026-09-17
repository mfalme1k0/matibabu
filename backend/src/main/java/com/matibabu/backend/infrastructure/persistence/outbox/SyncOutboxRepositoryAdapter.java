package com.matibabu.backend.infrastructure.persistence.outbox;

import com.matibabu.backend.synchronization.outbox.SyncOutboxEntry;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SyncOutboxRepositoryAdapter implements SyncOutboxRepository {

    private final SpringDataSyncOutboxRepository jpaRepository;
    private final SyncOutboxMapper mapper;

    public SyncOutboxRepositoryAdapter(
            SpringDataSyncOutboxRepository jpaRepository,
            SyncOutboxMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public SyncOutboxEntry save(SyncOutboxEntry entry) {
        SyncOutboxEntity entity = mapper.toEntity(entry);
        SyncOutboxEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<SyncOutboxEntry> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
}
