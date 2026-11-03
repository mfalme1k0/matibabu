CREATE TABLE encounters (
                            id CHAR(36) PRIMARY KEY,
                            patient_id CHAR(36) NOT NULL,
                            started_at TIMESTAMP NOT NULL,
                            status VARCHAR(20) NOT NULL,
                            ended_at TIMESTAMP
);