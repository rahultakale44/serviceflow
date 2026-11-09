package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.VehicleRequestDto;
import com.serviceflow.backend.dto.VehicleResponseDto;
import com.serviceflow.backend.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for vehicle management operations.
 * Provides CRUD endpoints for vehicle resources under /api/v1/vehicles.
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Slf4j
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * Create a new vehicle.
     *
     * @param requestDto vehicle data
     * @return created vehicle with 201 status
     */
    @PostMapping
    public ResponseEntity<VehicleResponseDto> createVehicle(
            @Valid @RequestBody VehicleRequestDto requestDto) {
        log.info("POST /api/v1/vehicles - Creating new vehicle");
        VehicleResponseDto response = vehicleService.createVehicle(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a vehicle by ID.
     *
     * @param id vehicle ID
     * @return vehicle data with 200 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> getVehicleById(@PathVariable Long id) {
        log.info("GET /api/v1/vehicles/{} - Fetching vehicle", id);
        VehicleResponseDto response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all vehicles with pagination.
     *
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @param sortBy field to sort by (default "id")
     * @param direction sort direction (default "ASC")
     * @return paginated list of vehicles with 200 status
     */
    @GetMapping
    public ResponseEntity<Page<VehicleResponseDto>> getAllVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("GET /api/v1/vehicles - Fetching vehicles (page: {}, size: {})", page, size);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<VehicleResponseDto> response = vehicleService.getAllVehicles(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all vehicles belonging to a specific customer.
     *
     * @param customerId customer ID
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @param sortBy field to sort by (default "id")
     * @param direction sort direction (default "ASC")
     * @return paginated list of customer's vehicles with 200 status
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<VehicleResponseDto>> getVehiclesByCustomerId(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("GET /api/v1/vehicles/customer/{} - Fetching vehicles for customer", customerId);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<VehicleResponseDto> response = vehicleService.getVehiclesByCustomerId(customerId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing vehicle.
     *
     * @param id vehicle ID
     * @param requestDto updated vehicle data
     * @return updated vehicle with 200 status
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequestDto requestDto) {
        log.info("PUT /api/v1/vehicles/{} - Updating vehicle", id);
        VehicleResponseDto response = vehicleService.updateVehicle(id, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a vehicle by ID.
     *
     * @param id vehicle ID
     * @return 204 No Content status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        log.info("DELETE /api/v1/vehicles/{} - Deleting vehicle", id);
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
