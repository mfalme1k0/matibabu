package com.matibabu.backend.security.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "clinicians")
public class Clinician {
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id = UuidCreator.getTimeOrderedEpoch();

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    public Clinician() { }

    public Clinician(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Only Getters, because ID is being created here so can't reset it later
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
