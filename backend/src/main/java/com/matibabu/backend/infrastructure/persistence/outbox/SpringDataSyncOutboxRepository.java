package com.matibabu.backend.infrastructure.persistence.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataSyncOutboxRepository extends JpaRepository<SyncOutboxEntity, UUID> {
}
