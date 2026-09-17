package com.matibabu.backend.application.referral;

import com.matibabu.backend.domain.encounter.Encounter;
import com.matibabu.backend.domain.encounter.EncounterRepository;
import com.matibabu.backend.domain.facility.Facility;
import com.matibabu.backend.domain.facility.FacilityRepository;
import com.matibabu.backend.domain.medicalrecord.MedicalRecordRepository;
import com.matibabu.backend.domain.referral.Referral;
import com.matibabu.backend.domain.referral.ReferralRepository;
import com.matibabu.backend.domain.referral.ReferralUrgency;
import com.matibabu.backend.exception.ReferralNotFoundException;
import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReferralServicesTest {

    @Test
    void createReferralRecordsOutboxEntry() {
        ReferralRepository referralRepository = mock(ReferralRepository.class);
        EncounterRepository encounterRepository = mock(EncounterRepository.class);
        MedicalRecordRepository medicalRecordRepository = mock(MedicalRecordRepository.class);
        FacilityRepository facilityRepository = mock(FacilityRepository.class);
        SyncOutboxRecorder syncOutboxRecorder = mock(SyncOutboxRecorder.class);

        UUID encounterId = UUID.randomUUID();
        UUID referringClinicianId = UUID.randomUUID();
        UUID receivingFacilityId = UUID.randomUUID();

        Encounter encounter = mock(Encounter.class);
        when(encounter.getPatientId()).thenReturn(UUID.randomUUID());
        when(encounterRepository.findById(encounterId)).thenReturn(Optional.of(encounter));
        when(facilityRepository.findById(receivingFacilityId)).thenReturn(Optional.of(mock(Facility.class)));
        when(referralRepository.save(any(Referral.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateReferralService service = new CreateReferralService(
                referralRepository, encounterRepository, medicalRecordRepository, facilityRepository, syncOutboxRecorder
        );

        Referral referral = service.create(
                encounterId, referringClinicianId, null, "Needs specialist review",
                ReferralUrgency.ROUTINE, receivingFacilityId, null
        );

        verify(syncOutboxRecorder).record(
                eq(AggregateType.REFERRAL), eq(referral.getId()), eq("ReferralCreated"), any()
        );
    }

    @Test
    void completeReferralRecordsOutboxEntry() {
        ReferralRepository referralRepository = mock(ReferralRepository.class);
        SyncOutboxRecorder syncOutboxRecorder = mock(SyncOutboxRecorder.class);

        UUID referralId = UUID.randomUUID();
        UUID resolvedBy = UUID.randomUUID();
        Referral referral = referralFixture(referralId);

        when(referralRepository.findById(referralId)).thenReturn(Optional.of(referral));
        when(referralRepository.save(referral)).thenReturn(referral);

        CompleteReferralService service = new CompleteReferralService(referralRepository, syncOutboxRecorder);
        service.complete(referralId, resolvedBy);

        verify(syncOutboxRecorder).record(
                eq(AggregateType.REFERRAL), eq(referralId), eq("ReferralCompleted"), any()
        );
    }

    @Test
    void cancelReferralRecordsOutboxEntry() {
        ReferralRepository referralRepository = mock(ReferralRepository.class);
        SyncOutboxRecorder syncOutboxRecorder = mock(SyncOutboxRecorder.class);

        UUID referralId = UUID.randomUUID();
        UUID resolvedBy = UUID.randomUUID();
        Referral referral = referralFixture(referralId);

        when(referralRepository.findById(referralId)).thenReturn(Optional.of(referral));
        when(referralRepository.save(referral)).thenReturn(referral);

        CancelReferralService service = new CancelReferralService(referralRepository, syncOutboxRecorder);
        service.cancel(referralId, resolvedBy);

        verify(syncOutboxRecorder).record(
                eq(AggregateType.REFERRAL), eq(referralId), eq("ReferralCancelled"), any()
        );
    }

    @Test
    void completeReferralThrowsWhenNotFoundAndRecordsNothing() {
        ReferralRepository referralRepository = mock(ReferralRepository.class);
        SyncOutboxRecorder syncOutboxRecorder = mock(SyncOutboxRecorder.class);
        UUID referralId = UUID.randomUUID();

        when(referralRepository.findById(referralId)).thenReturn(Optional.empty());

        CompleteReferralService service = new CompleteReferralService(referralRepository, syncOutboxRecorder);

        assertThrows(ReferralNotFoundException.class, () -> service.complete(referralId, UUID.randomUUID()));
        verifyNoInteractions(syncOutboxRecorder);
    }

    private static Referral referralFixture(UUID id) {
        return Referral.reconstitute(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "Needs specialist review",
                ReferralUrgency.ROUTINE,
                UUID.randomUUID(),
                null,
                com.matibabu.backend.domain.referral.ReferralStatus.PENDING,
                Instant.now(),
                null,
                null
        );
    }
}
