CREATE TABLE departments (
                             id CHAR(36) NOT NULL PRIMARY KEY,
                             facility_id CHAR(36) NOT NULL REFERENCES facilities(id),
                             code VARCHAR(50) NOT NULL,
                             name VARCHAR(255) NOT NULL,
                             active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX ix_departments_facility_id
    ON departments (facility_id);

CREATE UNIQUE INDEX ux_departments_facility_code
    ON departments (facility_id, code);