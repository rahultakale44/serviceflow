package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.TechnicianRequestDto;
import com.serviceflow.backend.dto.TechnicianResponseDto;
import com.serviceflow.backend.dto.TechnicianStatusUpdateDto;
import com.serviceflow.backend.entity.TechnicianSpecialization;
import com.serviceflow.backend.entity.TechnicianStatus;
import com.serviceflow.backend.service.TechnicianService;
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
 * REST Controller for technician management operations.
 * Provides endpoints for technician CRUD operations and filtering.
 */
@RestController
@RequestMapping("/api/v1/technicians")
@RequiredArgsConstructor
@Slf4j
public class TechnicianController {

    private final TechnicianService technicianService;

    /**
     * Create a new technician.
     *
     * @param requestDto technician data
     * @return created technician with 201 status
     */
    @PostMapping
    public ResponseEntity<TechnicianResponseDto> createTechnician(
            @Valid @RequestBody TechnicianRequestDto requestDto) {
        log.info("POST /api/v1/technicians - Creating new technician");
        TechnicianResponseDto response = technicianService.createTechnician(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a technician by ID.
     *
     * @param id technician ID
     * @return technician data with 200 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<TechnicianResponseDto> getTechnicianById(@PathVariable Long id) {
        log.info("GET /api/v1/technicians/{} - Fetching technician", id);
        TechnicianResponseDto response = technicianService.getTechnicianById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all technicians with pagination and optional filtering.
     *
     * @param status filter by status (optional)
     * @param specialization filter by specialization (optional)
     * @param activeOnly show only active technicians (default false)
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @param sortBy field to sort by (default "technicianCode")
     * @param direction sort direction (default "ASC")
     * @return paginated list of technicians with 200 status
     */
    @GetMapping
    public ResponseEntity<Page<TechnicianResponseDto>> getAllTechnicians(
            @RequestParam(required = false) TechnicianStatus status,
            @RequestParam(required = false) TechnicianSpecialization specialization,
            @RequestParam(defaultValue = "false") boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "technicianCode") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("GET /api/v1/technicians - Fetching technicians");

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<TechnicianResponseDto> response;

        if (activeOnly) {
            response = technicianService.getActiveTechnicians(pageable);
        } else if (status != null) {
            response = technicianService.getTechniciansByStatus(status, pageable);
        } else if (specialization != null) {
            response = technicianService.getTechniciansBySpecialization(specialization, pageable);
        } else {
            response = technicianService.getAllTechnicians(pageable);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing technician.
     *
     * @param id technician ID
     * @param requestDto updated technician data
     * @return updated technician with 200 status
     */
    @PutMapping("/{id}")
    public ResponseEntity<TechnicianResponseDto> updateTechnician(
            @PathVariable Long id,
            @Valid @RequestBody TechnicianRequestDto requestDto) {
        log.info("PUT /api/v1/technicians/{} - Updating technician", id);
        TechnicianResponseDto response = technicianService.updateTechnician(id, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Update technician status.
     *
     * @param id technician ID
     * @param statusDto status update data
     * @return updated technician with 200 status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TechnicianResponseDto> updateTechnicianStatus(
            @PathVariable Long id,
            @Valid @RequestBody TechnicianStatusUpdateDto statusDto) {
        log.info("PATCH /api/v1/technicians/{}/status - Updating technician status", id);
        TechnicianResponseDto response = technicianService.updateTechnicianStatus(id, statusDto);
        return ResponseEntity.ok(response);
    }
}
