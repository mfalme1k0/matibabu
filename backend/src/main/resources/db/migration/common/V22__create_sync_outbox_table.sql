

CREATE TABLE sync_outbox (
                             id CHAR(36) NOT NULL PRIMARY KEY,
                             facility_id CHAR(36) NOT NULL,
                             aggregate_type VARCHAR(20) NOT NULL,
                             aggregate_id CHAR(36) NOT NULL,
                             operation VARCHAR(50) NOT NULL,
                             payload TEXT NOT NULL,
                             created_at TIMESTAMP NOT NULL,
                             synced_at TIMESTAMP
);

CREATE INDEX ix_sync_outbox_facility_id ON sync_outbox (facility_id);
CREATE INDEX ix_sync_outbox_aggregate ON sync_outbox (aggregate_type, aggregate_id);
