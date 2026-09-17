package com.matibabu.backend.application.referral;

import com.matibabu.backend.domain.referral.Referral;
import com.matibabu.backend.domain.referral.ReferralRepository;
import com.matibabu.backend.exception.ReferralNotFoundException;
import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class CompleteReferralService implements CompleteReferralUseCase {

    private final ReferralRepository referralRepository;
    private final SyncOutboxRecorder syncOutboxRecorder;

    public CompleteReferralService(ReferralRepository referralRepository, SyncOutboxRecorder syncOutboxRecorder) {
        this.referralRepository = referralRepository;
        this.syncOutboxRecorder = syncOutboxRecorder;
    }

    @Override
    @Transactional
    public Referral complete(UUID referralId, UUID resolvedBy) {
        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new ReferralNotFoundException(referralId));

        Instant resolvedAt = Instant.now();
        referral.complete(resolvedBy, resolvedAt);
        Referral saved = referralRepository.save(referral);

        syncOutboxRecorder.record(
                AggregateType.REFERRAL,
                referralId,
                "ReferralCompleted",
                Map.of(
                        "resolvedBy", resolvedBy.toString(),
                        "resolvedAt", resolvedAt.toString()
                )
        );

        return saved;
    }
}
