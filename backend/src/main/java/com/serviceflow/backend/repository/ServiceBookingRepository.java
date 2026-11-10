package com.serviceflow.backend.repository;

import com.serviceflow.backend.entity.ServiceBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for ServiceBooking entity.
 * Provides database access methods for booking operations.
 */
@Repository
public interface ServiceBookingRepository extends JpaRepository<ServiceBooking, Long> {

    /**
     * Find booking by booking number.
     */
    Optional<ServiceBooking> findByBookingNumber(String bookingNumber);

    /**
     * Check if booking number exists.
     */
    boolean existsByBookingNumber(String bookingNumber);

    /**
     * Find all bookings for a specific customer with pagination.
     */
    @Query("SELECT b FROM ServiceBooking b WHERE b.customer.id = :customerId")
    Page<ServiceBooking> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * Find all bookings for a specific vehicle with pagination.
     */
    @Query("SELECT b FROM ServiceBooking b WHERE b.vehicle.id = :vehicleId")
    Page<ServiceBooking> findByVehicleId(@Param("vehicleId") Long vehicleId, Pageable pageable);

    /**
     * Count bookings for a specific customer.
     */
    @Query("SELECT COUNT(b) FROM ServiceBooking b WHERE b.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);

    /**
     * Count bookings for a specific vehicle.
     */
    @Query("SELECT COUNT(b) FROM ServiceBooking b WHERE b.vehicle.id = :vehicleId")
    long countByVehicleId(@Param("vehicleId") Long vehicleId);
}
