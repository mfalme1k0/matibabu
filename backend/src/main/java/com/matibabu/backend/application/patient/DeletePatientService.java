package com.matibabu.backend.application.patient;

import com.matibabu.backend.domain.patient.PatientRepository;
import com.matibabu.backend.exception.PatientNotFoundException;
import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

public class DeletePatientService implements DeletePatientUseCase {

    private final PatientRepository patientRepository;
    private final SyncOutboxRecorder syncOutboxRecorder;

    public DeletePatientService(PatientRepository patientRepository, SyncOutboxRecorder syncOutboxRecorder) {
        this.patientRepository = patientRepository;
        this.syncOutboxRecorder = syncOutboxRecorder;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException(id);
        }
        patientRepository.deleteById(id);

        syncOutboxRecorder.record(AggregateType.PATIENT, id, "PatientDeleted", Map.of());
    }
}
