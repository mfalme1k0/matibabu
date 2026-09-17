-- A durable record of every committed write to a facility-owned
-- clinical aggregate, waiting to be pushed to the central hub. See
-- docs/decisions/ADR-08-synchronization.md.
--
-- No foreign key on aggregate_id: it is a loose polymorphic reference
-- across four different tables (patients/encounters/medical_records/
-- referrals), depending on aggregate_type.
--
-- No separate sequence column: id is a time-ordered UUID v7 (ADR-004),
-- so ORDER BY id already reflects recording order.

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
