package com.serviceflow.backend.repository;

import com.serviceflow.backend.entity.Technician;
import com.serviceflow.backend.entity.TechnicianSpecialization;
import com.serviceflow.backend.entity.TechnicianStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Technician entity.
 * Provides database access methods with custom queries for filtering.
 */
@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {

    /**
     * Find a technician by technician code.
     */
    Optional<Technician> findByTechnicianCode(String technicianCode);

    /**
     * Find a technician by email.
     */
    Optional<Technician> findByEmail(String email);

    /**
     * Check if a technician exists with the given code.
     */
    boolean existsByTechnicianCode(String technicianCode);

    /**
     * Check if a technician exists with the given email.
     */
    boolean existsByEmail(String email);

    /**
     * Find technicians by status.
     */
    Page<Technician> findByStatus(TechnicianStatus status, Pageable pageable);

    /**
     * Find technicians by specialization.
     */
    Page<Technician> findBySpecialization(TechnicianSpecialization specialization, Pageable pageable);

    /**
     * Find active technicians.
     */
    Page<Technician> findByActiveTrue(Pageable pageable);

    /**
     * Find active technicians by status.
     */
    Page<Technician> findByActiveTrueAndStatus(TechnicianStatus status, Pageable pageable);

    /**
     * Find active technicians by specialization.
     */
    Page<Technician> findByActiveTrueAndSpecialization(TechnicianSpecialization specialization, Pageable pageable);
}
