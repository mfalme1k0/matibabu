ALTER TABLE encounters
    ADD COLUMN department_id CHAR(36)
        REFERENCES departments(id);

CREATE INDEX ix_encounters_department_id
    ON encounters (department_id);