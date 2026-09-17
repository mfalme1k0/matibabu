package com.matibabu.backend.synchronization.outbox;

import java.util.Optional;
import java.util.UUID;

public interface SyncOutboxRepository {

    SyncOutboxEntry save(SyncOutboxEntry entry);

    Optional<SyncOutboxEntry> findById(UUID id);
}
