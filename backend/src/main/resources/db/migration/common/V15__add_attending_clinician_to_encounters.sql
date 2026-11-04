-- Records which clinician attended/performed each encounter.
-- Nullable to preserve existing rows recorded before this field
-- existed; every newly started encounter is required by the domain
-- layer to set it.

-- SQLite supports adding a nullable column to an existing table.
ALTER TABLE encounters
    ADD COLUMN attending_clinician_id CHAR(36);

-- SQLite cannot add a foreign-key constraint to an existing table
-- using ALTER TABLE ... ADD CONSTRAINT.
--
-- Referential integrity for this relationship is therefore enforced
-- by the application/domain layer for newly created encounters.

CREATE INDEX ix_encounters_attending_clinician
    ON encounters (attending_clinician_id);