package com.matibabu.backend.domain.facility;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FacilityRepository {

    Facility save(Facility facility);

    Optional<Facility> findById(UUID id);

    Optional<Facility> findByMflCode(String mflCode);

    boolean existsByMflCode(String mflCode);

    List<Facility> findAllActive();

    List<Facility> search(String query);

    boolean existsById(UUID facilityId);
}
