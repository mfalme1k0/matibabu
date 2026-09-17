package com.matibabu.backend.synchronization.outbox;

/*
 * The clinical aggregates ADR-08 scopes the sync outbox to. Facility
 * and Medicine are deliberately excluded: they are centrally-governed
 * reference data, not facility-generated clinical activity.
 */
public enum AggregateType {
    PATIENT,
    ENCOUNTER,
    MEDICAL_RECORD,
    REFERRAL
}
