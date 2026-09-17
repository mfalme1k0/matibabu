package com.matibabu.backend.config;

import com.matibabu.backend.application.patient.*;
import com.matibabu.backend.domain.patient.PatientRepository;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PatientConfiguration {

    @Bean
    public RegisterPatientUseCase registerPatientUseCase(
            PatientRepository patientRepository,
            SyncOutboxRecorder syncOutboxRecorder
    ) {
        return new RegisterPatientService(patientRepository, syncOutboxRecorder);
    }

    @Bean
    public GetPatientUseCase getPatientUseCase(
            PatientRepository patientRepository
    ) {
        return new GetPatientService(patientRepository);
    }

    @Bean
    public ListPatientsUseCase listPatientsUseCase(
            PatientRepository patientRepository
    ) {
        return new ListPatientsService(patientRepository);
    }

    @Bean
    public UpdatePatientUseCase updatePatientUseCase(
            PatientRepository patientRepository,
            SyncOutboxRecorder syncOutboxRecorder
    ) {
        return new UpdatePatientService(patientRepository, syncOutboxRecorder);
    }

    @Bean
    public DeletePatientUseCase deletePatientUseCase(
            PatientRepository patientRepository,
            SyncOutboxRecorder syncOutboxRecorder
    ) {
        return new DeletePatientService(patientRepository, syncOutboxRecorder);
    }

    @Bean
    public SearchPatientByPhoneNumberUseCase searchPatientByPhoneNumberUseCase(
            PatientRepository patientRepository
    ) {
        return new SearchPatientByPhoneNumberService(patientRepository);
    }
}