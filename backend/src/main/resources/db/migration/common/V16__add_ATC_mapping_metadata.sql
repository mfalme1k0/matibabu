-- Tracks provenance of the KEML -> ATC mapping so the UI/API can
-- distinguish a pharmacist-confirmed code from an unreviewed
-- auto-match, and so re-seeding a future KEML edition doesn't
-- silently overwrite reviewed data.
--
-- atc_mapping_status values:
--   CONFIRMED     - a pharmacist has reviewed and confirmed this code
--   AUTO_MATCHED  - matched automatically by name against the WHO ATC
--                   index; not yet reviewed
--   NEEDS_REVIEW  - matcher could not find a confident match
--                   (common for fixed-dose combinations)
--   UNMAPPED      - no ATC code available at all
--
-- keml_version records which KEML edition this row was seeded/last
-- confirmed against, e.g. "KEML 2023".

ALTER TABLE medicines ADD COLUMN atc_mapping_status VARCHAR(20) NOT NULL DEFAULT 'UNMAPPED';
ALTER TABLE medicines ADD COLUMN keml_version VARCHAR(20);

CREATE INDEX ix_medicines_atc_mapping_status ON medicines (atc_mapping_status);