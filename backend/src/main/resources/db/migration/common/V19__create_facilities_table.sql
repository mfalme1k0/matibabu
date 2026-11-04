CREATE TABLE facilities (
                            id CHAR(36) NOT NULL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            type VARCHAR(30) NOT NULL,
                            mfl_code VARCHAR(20),
                            active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX ix_facilities_name ON facilities (name);
CREATE UNIQUE INDEX ix_facilities_mfl_code ON facilities (mfl_code);