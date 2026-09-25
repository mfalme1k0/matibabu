package com.matibabu.backend.api.department;

import com.matibabu.backend.application.department.CreateDepartmentUseCase;
import com.matibabu.backend.application.department.DeactivateDepartmentUseCase;
import com.matibabu.backend.application.department.ListDepartmentUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/departments")
public class DepartmentController {
    private final CreateDepartmentUseCase createDepartmentUseCase;
    private final ListDepartmentUseCase listDepartmentUseCase;
    private final DeactivateDepartmentUseCase deactivateDepartmentUseCase;

    public DepartmentController(
            CreateDepartmentUseCase createDepartmentUseCase,
            ListDepartmentUseCase listDepartmentUseCase,
            DeactivateDepartmentUseCase deactivateDepartmentUseCase
    ){
        this.createDepartmentUseCase = createDepartmentUseCase;
        this.listDepartmentUseCase = listDepartmentUseCase;
        this.deactivateDepartmentUseCase = deactivateDepartmentUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse create (@RequestBody CreateDepartmentRequest request){
        return DepartmentResponse.from(
                createDepartmentUseCase.Create(
                        request.facilityId(),
                        request.code(),
                        request.name()
                )
        );
    }

}
