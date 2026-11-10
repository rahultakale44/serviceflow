package com.serviceflow.backend.repository;

import com.serviceflow.backend.entity.ServiceJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for ServiceJob entity.
 * Provides database access methods for job card operations.
 */
@Repository
public interface ServiceJobRepository extends JpaRepository<ServiceJob, Long> {

    /**
     * Find job by job card number.
     */
    Optional<ServiceJob> findByJobCardNumber(String jobCardNumber);

    /**
     * Check if job card number exists.
     */
    boolean existsByJobCardNumber(String jobCardNumber);

    /**
     * Find job by booking ID.
     */
    @Query("SELECT j FROM ServiceJob j WHERE j.booking.id = :bookingId")
    Optional<ServiceJob> findByBookingId(@Param("bookingId") Long bookingId);

    /**
     * Check if a job already exists for a booking.
     */
    @Query("SELECT COUNT(j) > 0 FROM ServiceJob j WHERE j.booking.id = :bookingId")
    boolean existsByBookingId(@Param("bookingId") Long bookingId);

    /**
     * Find all jobs for a specific vehicle with pagination.
     */
    @Query("SELECT j FROM ServiceJob j WHERE j.vehicle.id = :vehicleId ORDER BY j.createdAt DESC")
    Page<ServiceJob> findByVehicleId(@Param("vehicleId") Long vehicleId, Pageable pageable);

    /**
     * Count jobs for a specific vehicle.
     */
    @Query("SELECT COUNT(j) FROM ServiceJob j WHERE j.vehicle.id = :vehicleId")
    long countByVehicleId(@Param("vehicleId") Long vehicleId);
}
