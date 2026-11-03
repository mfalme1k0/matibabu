-- Local reference catalog of prescribable medicines, seeded from the
-- Kenya Essential Medicines List (KEML) and coded against the WHO
-- ATC classification. This is master/reference data: it is not
-- expected to be edited through the normal clinical workflow.

CREATE TABLE medicines (
                           id CHAR(36) NOT NULL PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           generic_name VARCHAR(255),
                           atc_code VARCHAR(20),
                           form VARCHAR(100),
                           strength VARCHAR(100),
                           keml_code VARCHAR(50),
                           active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX ix_medicines_name ON medicines (name);
CREATE INDEX ix_medicines_atc_code ON medicines (atc_code);
