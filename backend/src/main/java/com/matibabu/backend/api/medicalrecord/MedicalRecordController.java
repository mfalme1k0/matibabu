package com.matibabu.backend.api.medicalrecord;

import com.matibabu.backend.application.medicalrecord.AddClinicalObservation;
import com.matibabu.backend.application.medicalrecord.AddDiagnosis;
import com.matibabu.backend.application.medicalrecord.AddTreatment;
import com.matibabu.backend.application.medicalrecord.AddVital;
import com.matibabu.backend.application.medicalrecord.AddAllergy;
import com.matibabu.backend.application.medicalrecord.CreateMedicalRecordUseCase;
import com.matibabu.backend.application.medicalrecord.GetMedicalRecordUseCase;
import com.matibabu.backend.exception.MedicalRecordNotFoundException;
import com.matibabu.backend.domain.medicalrecord.DiagnosisType;
import com.matibabu.backend.domain.medicalrecord.MedicalRecord;
import com.matibabu.backend.domain.medicalrecord.VitalType;
import com.matibabu.backend.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/encounters/{encounterId}/medical-record")
public class MedicalRecordController {

    private final CreateMedicalRecordUseCase createMedicalRecordUseCase;
    private final GetMedicalRecordUseCase getMedicalRecordUseCase;
    private final AddVital addVital;
    private final AddClinicalObservation addClinicalObservation;
    private final AddDiagnosis addDiagnosis;
    private final AddTreatment addTreatment;
    private final AddAllergy addAllergy;

    public MedicalRecordController(
            CreateMedicalRecordUseCase createMedicalRecordUseCase,
            GetMedicalRecordUseCase getMedicalRecordUseCase,
            AddVital addVital,
            AddClinicalObservation addClinicalObservation,
            AddDiagnosis addDiagnosis,
            AddTreatment addTreatment,
            AddAllergy addAllergy
    ) {
        this.createMedicalRecordUseCase = createMedicalRecordUseCase;
        this.getMedicalRecordUseCase = getMedicalRecordUseCase;
        this.addVital = addVital;
        this.addClinicalObservation = addClinicalObservation;
        this.addDiagnosis = addDiagnosis;
        this.addTreatment = addTreatment;
        this.addAllergy = addAllergy;
    }

    /*
     * Creates a medical record for an existing encounter.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicalRecordResponse create(
            @PathVariable UUID encounterId
    ) {
        MedicalRecord medicalRecord =
                createMedicalRecordUseCase.create(encounterId);

        return MedicalRecordResponse.from(medicalRecord);
    }

    /*
     * Retrieves the medical record belonging to an encounter.
     */
    @GetMapping
    public MedicalRecordResponse get(
            @PathVariable UUID encounterId
    ) {
        return getMedicalRecordUseCase
                .getByEncounterId(encounterId)
                .map(MedicalRecordResponse::from)
                .orElseThrow(
                        () -> new MedicalRecordNotFoundException(encounterId)
                );
    }

    /*
     * Adds a vital to the medical record belonging to the encounter.
     */
    @PostMapping("/vitals")
    public ResponseEntity<MedicalRecordResponse> addVital(
            @PathVariable UUID encounterId,
            @RequestParam VitalType type,
            @RequestParam String value,
            @RequestParam String unit
    ) {
        MedicalRecord medicalRecord =
                getMedicalRecordUseCase
                        .getByEncounterId(encounterId)
                        .orElseThrow(
                                () -> new MedicalRecordNotFoundException(encounterId)
                        );

        MedicalRecord updated =
                addVital.execute(
                        medicalRecord.getId(),
                        type,
                        value,
                        unit
                );

        return ResponseEntity.ok(MedicalRecordResponse.from(updated));
    }

    /*
     * Adds a clinical observation to the medical record.
     */
    @PostMapping("/observations")
    public ResponseEntity<MedicalRecordResponse> addObservation(
            @PathVariable UUID encounterId,
            @RequestParam String description
    ) {
        MedicalRecord medicalRecord =
                getMedicalRecordUseCase
                        .getByEncounterId(encounterId)
                        .orElseThrow(
                                () -> new MedicalRecordNotFoundException(encounterId)
                        );

        MedicalRecord updated =
                addClinicalObservation.execute(
                        medicalRecord.getId(),
                        description
                );

        return ResponseEntity.ok(MedicalRecordResponse.from(updated));
    }

    /*
     * Adds a diagnosis to the medical record.
     */
    @PostMapping("/diagnoses")
    public ResponseEntity<MedicalRecordResponse> addDiagnosis(
            @PathVariable UUID encounterId,
            @RequestParam String description,
            @RequestParam DiagnosisType type
    ) {
        MedicalRecord medicalRecord =
                getMedicalRecordUseCase
                        .getByEncounterId(encounterId)
                        .orElseThrow(
                                () -> new MedicalRecordNotFoundException(encounterId)
                        );

        MedicalRecord updated =
                addDiagnosis.execute(
                        medicalRecord.getId(),
                        description,
                        type
                );

        return ResponseEntity.ok(MedicalRecordResponse.from(updated));
    }

    /*
     * Adds a treatment to the medical record.
     *
     * The prescribing clinician is taken from the authenticated
     * session, not from the request body: who performed the
     * treatment must not be something the client can spoof.
     */
    @PostMapping("/treatments")
    public ResponseEntity<MedicalRecordResponse> addTreatment(
            @PathVariable UUID encounterId,
            @RequestBody AddTreatmentRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        MedicalRecord medicalRecord =
                getMedicalRecordUseCase
                        .getByEncounterId(encounterId)
                        .orElseThrow(
                                () -> new MedicalRecordNotFoundException(encounterId)
                        );

        MedicalRecord updated =
                addTreatment.execute(
                        medicalRecord.getId(),
                        request.medicineId(),
                        principal.getClinician().getId(),
                        request.dose(),
                        request.doseUnit(),
                        request.route(),
                        request.frequency(),
                        request.durationDays(),
                        request.notes()
                );

        return ResponseEntity.ok(MedicalRecordResponse.from(updated));
    }
    
        /*
        * Adds an allergy to the medical record belonging to the encounter.
        */
        @PostMapping("/allergies")
        public ResponseEntity<MedicalRecordResponse> addAllergy(
                @PathVariable UUID encounterId,
                @RequestParam String allergen,
                @RequestParam String reaction
        ) {
        MedicalRecord medicalRecord =
                getMedicalRecordUseCase
                        .getByEncounterId(encounterId)
                        .orElseThrow(
                                () -> new MedicalRecordNotFoundException(encounterId)
                        );

        MedicalRecord updated =
                addAllergy.execute(
                        medicalRecord.getId(),
                        allergen,
                        reaction
                );

        return ResponseEntity.ok(MedicalRecordResponse.from(updated));
        }

}
