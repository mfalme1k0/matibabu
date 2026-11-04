-- V26: Change the legacy SQLite clinicians.id declaration from BLOB to CHAR(36).
--
-- Existing databases may contain text UUID values in a column declared as BLOB.
-- Hibernate validates the declared SQLite type and expects CHAR(36), so rebuild
-- the table while preserving all existing clinician data.


CREATE TABLE clinicians_new (
                                id CHAR(36) PRIMARY KEY,
                                email VARCHAR(255) NOT NULL UNIQUE,
                                password VARCHAR(255) NOT NULL,
                                role VARCHAR(20) NOT NULL
);

INSERT INTO clinicians_new (
    id,
    email,
    password,
    role
)
SELECT
    id,
    email,
    password,
    role
FROM clinicians;

DROP TABLE clinicians;

ALTER TABLE clinicians_new
    RENAME TO clinicians;
