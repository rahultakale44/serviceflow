package com.serviceflow.backend.repository;

import com.serviceflow.backend.entity.BayType;
import com.serviceflow.backend.entity.ServiceBay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for ServiceBay entity.
 * Provides database access methods with filtering capabilities.
 */
@Repository
public interface ServiceBayRepository extends JpaRepository<ServiceBay, Long> {

    /**
     * Find a service bay by bay code.
     */
    Optional<ServiceBay> findByBayCode(String bayCode);

    /**
     * Check if a service bay exists with the given code.
     */
    boolean existsByBayCode(String bayCode);

    /**
     * Find service bays by type.
     */
    Page<ServiceBay> findByBayType(BayType bayType, Pageable pageable);

    /**
     * Find active service bays.
     */
    Page<ServiceBay> findByActiveTrue(Pageable pageable);

    /**
     * Find active service bays by type.
     */
    Page<ServiceBay> findByActiveTrueAndBayType(BayType bayType, Pageable pageable);
}
