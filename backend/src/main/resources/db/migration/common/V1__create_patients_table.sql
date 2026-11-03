CREATE TABLE patients (
                          id CHAR(36) PRIMARY KEY,
                          first_name VARCHAR(255) NOT NULL,
                          last_name VARCHAR(255) NOT NULL,
                          date_of_birth DATE NOT NULL,
                          phone_number VARCHAR(50) NOT NULL,
                          gender VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP NOT NULL
);