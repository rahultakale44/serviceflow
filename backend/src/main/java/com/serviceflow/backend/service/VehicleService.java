package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.VehicleRequestDto;
import com.serviceflow.backend.dto.VehicleResponseDto;
import com.serviceflow.backend.entity.Customer;
import com.serviceflow.backend.entity.Vehicle;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.repository.CustomerRepository;
import com.serviceflow.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for vehicle management operations.
 * Implements business logic, validation, and transaction management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;

    /**
     * Create a new vehicle for an existing customer.
     * Validates uniqueness of registration number and VIN before creation.
     *
     * @param requestDto the vehicle data
     * @return the created vehicle response
     * @throws ResourceNotFoundException if customer not found
     * @throws DuplicateResourceException if registration number or VIN already exists
     */
    @Transactional
    public VehicleResponseDto createVehicle(VehicleRequestDto requestDto) {
        log.info("Creating vehicle with registration: {}", requestDto.getRegistrationNumber());

        // Validate customer exists
        Customer customer = customerRepository.findById(requestDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", requestDto.getCustomerId()));

        // Normalize registration number
        String normalizedRegistration = normalizeRegistrationNumber(requestDto.getRegistrationNumber());

        // Validate registration number uniqueness
        if (vehicleRepository.existsByRegistrationNumber(normalizedRegistration)) {
            throw new DuplicateResourceException("Registration number", normalizedRegistration);
        }

        // Normalize and validate VIN uniqueness (only if VIN is provided and not blank)
        String normalizedVin = normalizeVin(requestDto.getVin());
        if (normalizedVin != null && vehicleRepository.existsByVin(normalizedVin)) {
            throw new DuplicateResourceException("VIN", normalizedVin);
        }

        Vehicle vehicle = Vehicle.builder()
                .registrationNumber(normalizedRegistration)
                .make(requestDto.getMake())
                .model(requestDto.getModel())
                .manufacturingYear(requestDto.getManufacturingYear())
                .fuelType(requestDto.getFuelType())
                .vin(normalizedVin)
                .customer(customer)
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created successfully with id: {}", savedVehicle.getId());

        return mapToResponseDto(savedVehicle);
    }

    /**
     * Get a vehicle by ID.
     *
     * @param id the vehicle ID
     * @return the vehicle response
     * @throws ResourceNotFoundException if vehicle not found
     */
    @Transactional(readOnly = true)
    public VehicleResponseDto getVehicleById(Long id) {
        log.info("Fetching vehicle with id: {}", id);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
        return mapToResponseDto(vehicle);
    }

    /**
     * Get all vehicles with pagination.
     *
     * @param pageable pagination parameters
     * @return page of vehicle responses
     */
    @Transactional(readOnly = true)
    public Page<VehicleResponseDto> getAllVehicles(Pageable pageable) {
        log.info("Fetching vehicles with pagination: {}", pageable);
        return vehicleRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }

    /**
     * Get all vehicles belonging to a specific customer.
     *
     * @param customerId the customer ID
     * @param pageable pagination parameters
     * @return page of vehicle responses
     * @throws ResourceNotFoundException if customer not found
     */
    @Transactional(readOnly = true)
    public Page<VehicleResponseDto> getVehiclesByCustomerId(Long customerId, Pageable pageable) {
        log.info("Fetching vehicles for customer id: {}", customerId);

        // Validate customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", customerId);
        }

        return vehicleRepository.findByCustomerId(customerId, pageable)
                .map(this::mapToResponseDto);
    }

    /**
     * Update an existing vehicle.
     * Validates uniqueness of registration number and VIN if changed.
     * Prevents ownership changes.
     *
     * @param id the vehicle ID
     * @param requestDto the updated vehicle data
     * @return the updated vehicle response
     * @throws ResourceNotFoundException if vehicle not found
     * @throws DuplicateResourceException if registration number or VIN conflicts with another vehicle
     */
    @Transactional
    public VehicleResponseDto updateVehicle(Long id, VehicleRequestDto requestDto) {
        log.info("Updating vehicle with id: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));

        // Prevent ownership changes
        if (!vehicle.getCustomer().getId().equals(requestDto.getCustomerId())) {
            log.warn("Attempted to change vehicle {} ownership from customer {} to {}",
                    id, vehicle.getCustomer().getId(), requestDto.getCustomerId());
            throw new DuplicateResourceException(
                "Cannot change vehicle ownership. Customer ID must remain " + vehicle.getCustomer().getId()
            );
        }

        // Normalize and check registration number uniqueness if changed
        String normalizedRegistration = normalizeRegistrationNumber(requestDto.getRegistrationNumber());
        if (!vehicle.getRegistrationNumber().equals(normalizedRegistration)) {
            if (vehicleRepository.existsByRegistrationNumber(normalizedRegistration)) {
                throw new DuplicateResourceException("Registration number", normalizedRegistration);
            }
            vehicle.setRegistrationNumber(normalizedRegistration);
        }

        // Normalize and check VIN uniqueness if changed
        String normalizedVin = normalizeVin(requestDto.getVin());
        String currentVin = vehicle.getVin();

        // Only check if the VIN is actually changing
        if ((normalizedVin == null && currentVin != null) ||
            (normalizedVin != null && !normalizedVin.equals(currentVin))) {

            if (normalizedVin != null && vehicleRepository.existsByVin(normalizedVin)) {
                throw new DuplicateResourceException("VIN", normalizedVin);
            }
            vehicle.setVin(normalizedVin);
        }

        // Update other fields
        vehicle.setMake(requestDto.getMake());
        vehicle.setModel(requestDto.getModel());
        vehicle.setManufacturingYear(requestDto.getManufacturingYear());
        vehicle.setFuelType(requestDto.getFuelType());

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle updated successfully with id: {}", updatedVehicle.getId());

        return mapToResponseDto(updatedVehicle);
    }

    /**
     * Delete a vehicle by ID.
     * Does not delete the associated customer.
     *
     * @param id the vehicle ID
     * @throws ResourceNotFoundException if vehicle not found
     */
    @Transactional
    public void deleteVehicle(Long id) {
        log.info("Deleting vehicle with id: {}", id);

        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle", id);
        }

        vehicleRepository.deleteById(id);
        log.info("Vehicle deleted successfully with id: {}", id);
    }

    /**
     * Map Vehicle entity to VehicleResponseDto.
     */
    private VehicleResponseDto mapToResponseDto(Vehicle vehicle) {
        Customer customer = vehicle.getCustomer();

        VehicleResponseDto.CustomerSummaryDto customerSummary = VehicleResponseDto.CustomerSummaryDto.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .build();

        return VehicleResponseDto.builder()
                .id(vehicle.getId())
                .registrationNumber(vehicle.getRegistrationNumber())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .manufacturingYear(vehicle.getManufacturingYear())
                .fuelType(vehicle.getFuelType())
                .vin(vehicle.getVin())
                .customer(customerSummary)
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }

    /**
     * Normalize registration number: convert to uppercase and trim whitespace.
     */
    private String normalizeRegistrationNumber(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.isBlank()) {
            return null;
        }
        return registrationNumber.trim().toUpperCase();
    }

    /**
     * Normalize VIN: convert to uppercase, trim whitespace, convert blank to null.
     */
    private String normalizeVin(String vin) {
        if (vin == null || vin.isBlank()) {
            return null;
        }
        return vin.trim().toUpperCase();
    }
}
