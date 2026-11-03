-- Audit trail for who resolved an ATC mapping and when, so a
-- NEEDS_REVIEW/UNMAPPED entry's resolution is traceable back to a
-- specific admin, not just a status flip.

ALTER TABLE medicines ADD COLUMN reviewed_by CHAR(36);
ALTER TABLE medicines ADD COLUMN reviewed_at TIMESTAMP;

-- SQLite cannot add a foreign-key constraint to an existing table
-- using ALTER TABLE ... ADD CONSTRAINT (see V15 for the same issue).
--
-- Referential integrity for reviewed_by -> clinicians(id) is
-- therefore enforced by the application/domain layer
-- (ResolveAtcMappingService resolves the reviewer from the
-- authenticated session, not client input), not the database.

CREATE INDEX ix_medicines_reviewed_by ON medicines (reviewed_by);