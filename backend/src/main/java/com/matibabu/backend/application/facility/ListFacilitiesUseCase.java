package com.matibabu.backend.application.facility;

import com.matibabu.backend.domain.facility.Facility;

import java.util.List;
import java.util.UUID;

public interface ListFacilitiesUseCase {


    Facility getById(UUID id);



    List<Facility> listActive();

    List<Facility> search(String query);
}
