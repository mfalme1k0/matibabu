package com.matibabu.backend.application.patient;

import com.matibabu.backend.domain.patient.Gender;
import com.matibabu.backend.domain.patient.Patient;
import com.matibabu.backend.domain.patient.PatientRepository;
import com.matibabu.backend.exception.DuplicatePhoneNumberException;
import com.matibabu.backend.exception.PatientNotFoundException;
import com.matibabu.backend.synchronization.outbox.AggregateType;
import com.matibabu.backend.synchronization.outbox.SyncOutboxRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServicesTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private SyncOutboxRecorder syncOutboxRecorder;

    private RegisterPatientService registerPatientService;
    private GetPatientService getPatientService;
    private ListPatientsService listPatientsService;
    private UpdatePatientService updatePatientService;
    private DeletePatientService deletePatientService;
    private SearchPatientByPhoneNumberService searchPatientByPhoneNumberService;

    @BeforeEach
    void setUp() {
        registerPatientService = new RegisterPatientService(patientRepository, syncOutboxRecorder);
        getPatientService = new GetPatientService(patientRepository);
        listPatientsService = new ListPatientsService(patientRepository);
        updatePatientService = new UpdatePatientService(patientRepository, syncOutboxRecorder);
        deletePatientService = new DeletePatientService(patientRepository, syncOutboxRecorder);
        searchPatientByPhoneNumberService = new SearchPatientByPhoneNumberService(patientRepository);
    }

    @Test
    void registerPatientSavesAndReturnsPatient() {
        LocalDate dob = LocalDate.of(1995, 6, 15);
        when(patientRepository.existsByPhoneNumber("+254712345678")).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Patient patient = registerPatientService.register(
                "John",
                "Kamau",
                dob,
                Gender.MALE,
                "+254712345678",
                "Nairobi",
                null,
                null
        );

        assertNotNull(patient);
        assertEquals("John", patient.getFirstName());
        assertEquals("Kamau", patient.getLastName());
        assertEquals(Gender.MALE, patient.getGender());
        assertEquals("+254712345678", patient.getPhoneNumber());
        assertEquals("Nairobi", patient.getAddress());
        verify(patientRepository).existsByPhoneNumber("+254712345678");
        verify(patientRepository).save(any(Patient.class));
        verify(syncOutboxRecorder).record(eq(AggregateType.PATIENT), eq(patient.getId()), eq("PatientRegistered"), any());
    }

    @Test
    void registerPatientThrowsExceptionWhenPhoneNumberExists() {
        LocalDate dob = LocalDate.of(1995, 6, 15);
        when(patientRepository.existsByPhoneNumber("+254712345678")).thenReturn(true);

        assertThrows(DuplicatePhoneNumberException.class, () -> registerPatientService.register(
                "John",
                "Kamau",
                dob,
                Gender.MALE,
                "+254712345678",
                "Nairobi",
                null,
                null
        ));

        verify(patientRepository).existsByPhoneNumber("+254712345678");
        verify(patientRepository, never()).save(any(Patient.class));
        verifyNoInteractions(syncOutboxRecorder);
    }

    @Test
    void getByIdReturnsPatientWhenFound() {
        UUID id = UUID.randomUUID();
        Patient mockPatient = new Patient("John", "Kamau", LocalDate.of(1995, 6, 15), Gender.MALE, "+254712345678", "Nairobi", null, null);
        when(patientRepository.findById(id)).thenReturn(Optional.of(mockPatient));

        Patient patient = getPatientService.getById(id);

        assertNotNull(patient);
        assertEquals("John", patient.getFirstName());
        verify(patientRepository).findById(id);
    }

    @Test
    void getByIdThrowsExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(patientRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> getPatientService.getById(id));
        verify(patientRepository).findById(id);
    }

    @Test
    void listReturnsPaginatedPatients() {
        Pageable pageable = PageRequest.of(0, 20);
        Patient mockPatient = new Patient("John", "Kamau", LocalDate.of(1995, 6, 15), Gender.MALE, "+254712345678", "Nairobi", null, null);
        Page<Patient> page = new PageImpl<>(List.of(mockPatient), pageable, 1);
        when(patientRepository.findAll(pageable)).thenReturn(page);

        Page<Patient> result = listPatientsService.list(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(patientRepository).findAll(pageable);
    }

    @Test
    void updateModifiesExistingPatient() {
        UUID id = UUID.randomUUID();
        Patient existing = new Patient("John", "Kamau", LocalDate.of(1995, 6, 15), Gender.MALE, "+254712345678", "Nairobi", null, null);
        when(patientRepository.findById(id)).thenReturn(Optional.of(existing));
        when(patientRepository.findByPhoneNumber("+254700000000")).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Patient updated = updatePatientService.update(
                id,
                "John",
                "Kamau",
                LocalDate.of(1995, 6, 15),
                Gender.MALE,
                "+254700000000",
                "Mombasa",
                null,
                null
        );

        assertNotNull(updated);
        assertEquals("+254700000000", updated.getPhoneNumber());
        assertEquals("Mombasa", updated.getAddress());
        verify(patientRepository).findById(id);
        verify(patientRepository).findByPhoneNumber("+254700000000");
        verify(patientRepository).save(existing);
        verify(syncOutboxRecorder).record(eq(AggregateType.PATIENT), eq(existing.getId()), eq("PatientUpdated"), any());
    }

    @Test
    void updateThrowsExceptionWhenPhoneNumberBelongsToAnotherPatient() {
        UUID id = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        Patient existing = new Patient("John", "Kamau", LocalDate.of(1995, 6, 15), Gender.MALE, "+254712345678", "Nairobi", null, null);
        Patient other = new Patient("Jane", "Doe", LocalDate.of(1990, 1, 1), Gender.FEMALE, "+254700000000", "Kisumu", null, null);
        // Ensure other has otherId
        Patient otherWithId = Patient.reconstitute(
                otherId,
                other.getFirstName(),
                other.getLastName(),
                other.getDateOfBirth(),
                other.getGender(),
                other.getPhoneNumber(),
                other.getAddress(),
                other.getNationalId(),
                other.getBirthCertificateNumber(),
                other.getCreatedAt(),
                other.getUpdatedAt()
        );

        when(patientRepository.findById(id)).thenReturn(Optional.of(existing));
        when(patientRepository.findByPhoneNumber("+254700000000")).thenReturn(Optional.of(otherWithId));

        assertThrows(DuplicatePhoneNumberException.class, () -> updatePatientService.update(
                id,
                "John",
                "Kamau",
                LocalDate.of(1995, 6, 15),
                Gender.MALE,
                "+254700000000",
                "Mombasa",
                null,
                null
        ));

        verify(patientRepository).findById(id);
        verify(patientRepository).findByPhoneNumber("+254700000000");
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void updateWithSamePhoneNumberSucceeds() {
        UUID id = UUID.randomUUID();
        Patient existing = Patient.reconstitute(
                id,
                "John",
                "Kamau",
                LocalDate.of(1995, 6, 15),
                Gender.MALE,
                "+254712345678",
                "Nairobi",
                null,
                null,
                null,
                null
        );
        when(patientRepository.findById(id)).thenReturn(Optional.of(existing));
        when(patientRepository.findByPhoneNumber("+254712345678")).thenReturn(Optional.of(existing));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Patient updated = updatePatientService.update(
                id,
                "John",
                "Kamau",
                LocalDate.of(1995, 6, 15),
                Gender.MALE,
                "+254712345678",
                "Mombasa",
                null,
                null
        );

        assertNotNull(updated);
        assertEquals("Mombasa", updated.getAddress());
        verify(patientRepository).findById(id);
        verify(patientRepository).save(existing);
    }

    @Test
    void updateThrowsExceptionWhenPatientNotFound() {
        UUID id = UUID.randomUUID();
        when(patientRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> updatePatientService.update(
                id,
                "John",
                "Kamau",
                LocalDate.of(1995, 6, 15),
                Gender.MALE,
                "+254700000000",
                "Mombasa",
                null,
                null
        ));
        verify(patientRepository).findById(id);
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void deleteRemovesPatientWhenFound() {
        UUID id = UUID.randomUUID();
        when(patientRepository.existsById(id)).thenReturn(true);

        deletePatientService.delete(id);

        verify(patientRepository).existsById(id);
        verify(patientRepository).deleteById(id);
        verify(syncOutboxRecorder).record(eq(AggregateType.PATIENT), eq(id), eq("PatientDeleted"), any());
    }

    @Test
    void deleteThrowsExceptionWhenPatientNotFound() {
        UUID id = UUID.randomUUID();
        when(patientRepository.existsById(id)).thenReturn(false);

        assertThrows(PatientNotFoundException.class, () -> deletePatientService.delete(id));
        verify(patientRepository).existsById(id);
        verify(patientRepository, never()).deleteById(any());
    }

    @Test
    void searchByPhoneNumberReturnsPatientWhenFound() {
        Patient mockPatient = new Patient("John", "Kamau", LocalDate.of(1995, 6, 15), Gender.MALE, "+254712345678", "Nairobi", null, null);
        when(patientRepository.findByPhoneNumber("+254712345678")).thenReturn(Optional.of(mockPatient));

        Patient patient = searchPatientByPhoneNumberService.searchByPhoneNumber("+254712345678");

        assertNotNull(patient);
        assertEquals("John", patient.getFirstName());
        assertEquals("+254712345678", patient.getPhoneNumber());
        verify(patientRepository).findByPhoneNumber("+254712345678");
    }

    @Test
    void searchByPhoneNumberHandlesLeadingSpaceDecodedPlus() {
        Patient mockPatient = new Patient("John", "Kamau", LocalDate.of(1995, 6, 15), Gender.MALE, "+254712345678", "Nairobi", null, null);
        when(patientRepository.findByPhoneNumber(" 254712345678")).thenReturn(Optional.empty());
        when(patientRepository.findByPhoneNumber("254712345678")).thenReturn(Optional.empty());
        when(patientRepository.findByPhoneNumber("+254712345678")).thenReturn(Optional.of(mockPatient));

        Patient patient = searchPatientByPhoneNumberService.searchByPhoneNumber(" 254712345678");

        assertNotNull(patient);
        assertEquals("+254712345678", patient.getPhoneNumber());
    }

    @Test
    void searchByPhoneNumberHandlesKenyanLocalFormat() {
        Patient mockPatient = new Patient("John", "Kamau", LocalDate.of(1995, 6, 15), Gender.MALE, "+254712345678", "Nairobi", null, null);
        when(patientRepository.findByPhoneNumber("0712345678")).thenReturn(Optional.empty());
        when(patientRepository.findByPhoneNumber("+0712345678")).thenReturn(Optional.empty());
        when(patientRepository.findByPhoneNumber("+254712345678")).thenReturn(Optional.of(mockPatient));

        Patient patient = searchPatientByPhoneNumberService.searchByPhoneNumber("0712345678");

        assertNotNull(patient);
        assertEquals("+254712345678", patient.getPhoneNumber());
    }

    @Test
    void searchByPhoneNumberThrowsExceptionWhenNotFound() {
        when(patientRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> searchPatientByPhoneNumberService.searchByPhoneNumber("+254799999999"));
    }
}
