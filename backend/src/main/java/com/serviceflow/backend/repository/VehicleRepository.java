package com.serviceflow.backend.repository;

import com.serviceflow.backend.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Vehicle entity.
 * Provides CRUD operations and custom queries for vehicle data access.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Find a vehicle by registration number.
     *
     * @param registrationNumber the registration number to search for
     * @return Optional containing the vehicle if found
     */
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    /**
     * Find a vehicle by VIN.
     *
     * @param vin the VIN to search for
     * @return Optional containing the vehicle if found
     */
    Optional<Vehicle> findByVin(String vin);

    /**
     * Check if a vehicle exists with the given registration number.
     *
     * @param registrationNumber the registration number to check
     * @return true if a vehicle exists with this registration number
     */
    boolean existsByRegistrationNumber(String registrationNumber);

    /**
     * Check if a vehicle exists with the given VIN.
     *
     * @param vin the VIN to check
     * @return true if a vehicle exists with this VIN
     */
    boolean existsByVin(String vin);

    /**
     * Find all vehicles belonging to a specific customer.
     *
     * @param customerId the customer ID
     * @param pageable pagination parameters
     * @return page of vehicles
     */
    @Query("SELECT v FROM Vehicle v WHERE v.customer.id = :customerId")
    Page<Vehicle> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * Check if any vehicles exist for a given customer.
     *
     * @param customerId the customer ID
     * @return true if the customer has vehicles
     */
    @Query("SELECT COUNT(v) > 0 FROM Vehicle v WHERE v.customer.id = :customerId")
    boolean existsByCustomerId(@Param("customerId") Long customerId);
}
