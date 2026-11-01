package com.matibabu.backend.api.department;

import com.matibabu.backend.application.department.CreateDepartmentUseCase;
import com.matibabu.backend.application.department.DeactivateDepartmentUseCase;
import com.matibabu.backend.application.department.ListDepartmentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final CreateDepartmentUseCase createDepartmentUseCase;
    private final ListDepartmentUseCase listDepartmentUseCase;
    private final DeactivateDepartmentUseCase deactivateDepartmentUseCase;

    public DepartmentController(
            CreateDepartmentUseCase createDepartmentUseCase,
            ListDepartmentUseCase listDepartmentUseCase,
            DeactivateDepartmentUseCase deactivateDepartmentUseCase
    ) {
        this.createDepartmentUseCase = createDepartmentUseCase;
        this.listDepartmentUseCase = listDepartmentUseCase;
        this.deactivateDepartmentUseCase = deactivateDepartmentUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse create(@Valid @RequestBody CreateDepartmentRequest request) {
        return DepartmentResponse.from(
                createDepartmentUseCase.create(
                        request.facilityId(),
                        request.code(),
                        request.name()
                )
        );
    }

    @GetMapping("/{id}")
    public DepartmentResponse getById(@PathVariable UUID id) {
        return DepartmentResponse.from(listDepartmentUseCase.getById(id));
    }

    @GetMapping
    public List<DepartmentResponse> listByFacility(
            @RequestParam UUID facilityId,
            @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        List<com.matibabu.backend.domain.department.Department> departments = activeOnly
                ? listDepartmentUseCase.findAllActive(facilityId)
                : listDepartmentUseCase.findByFacilityId(facilityId);

        return departments.stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @PostMapping("/{id}/deactivate")
    public DepartmentResponse deactivate(@PathVariable UUID id) {
        return DepartmentResponse.from(deactivateDepartmentUseCase.deactivate(id));
    }
}
