package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.ServiceBayRequestDto;
import com.serviceflow.backend.dto.ServiceBayResponseDto;
import com.serviceflow.backend.dto.ServiceBayStatusUpdateDto;
import com.serviceflow.backend.entity.BayType;
import com.serviceflow.backend.service.ServiceBayService;
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
 * REST Controller for service bay management operations.
 * Provides endpoints for service bay CRUD operations and filtering.
 */
@RestController
@RequestMapping("/api/v1/service-bays")
@RequiredArgsConstructor
@Slf4j
public class ServiceBayController {

    private final ServiceBayService serviceBayService;

    /**
     * Create a new service bay.
     *
     * @param requestDto service bay data
     * @return created service bay with 201 status
     */
    @PostMapping
    public ResponseEntity<ServiceBayResponseDto> createServiceBay(
            @Valid @RequestBody ServiceBayRequestDto requestDto) {
        log.info("POST /api/v1/service-bays - Creating new service bay");
        ServiceBayResponseDto response = serviceBayService.createServiceBay(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a service bay by ID.
     *
     * @param id service bay ID
     * @return service bay data with 200 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceBayResponseDto> getServiceBayById(@PathVariable Long id) {
        log.info("GET /api/v1/service-bays/{} - Fetching service bay", id);
        ServiceBayResponseDto response = serviceBayService.getServiceBayById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all service bays with pagination and optional filtering.
     *
     * @param bayType filter by bay type (optional)
     * @param activeOnly show only active bays (default false)
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @param sortBy field to sort by (default "bayCode")
     * @param direction sort direction (default "ASC")
     * @return paginated list of service bays with 200 status
     */
    @GetMapping
    public ResponseEntity<Page<ServiceBayResponseDto>> getAllServiceBays(
            @RequestParam(required = false) BayType bayType,
            @RequestParam(defaultValue = "false") boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "bayCode") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("GET /api/v1/service-bays - Fetching service bays");

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<ServiceBayResponseDto> response;

        if (activeOnly) {
            response = serviceBayService.getActiveServiceBays(pageable);
        } else if (bayType != null) {
            response = serviceBayService.getServiceBaysByType(bayType, pageable);
        } else {
            response = serviceBayService.getAllServiceBays(pageable);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing service bay.
     *
     * @param id service bay ID
     * @param requestDto updated service bay data
     * @return updated service bay with 200 status
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceBayResponseDto> updateServiceBay(
            @PathVariable Long id,
            @Valid @RequestBody ServiceBayRequestDto requestDto) {
        log.info("PUT /api/v1/service-bays/{} - Updating service bay", id);
        ServiceBayResponseDto response = serviceBayService.updateServiceBay(id, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Update service bay status.
     *
     * @param id service bay ID
     * @param statusDto status update data
     * @return updated service bay with 200 status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceBayResponseDto> updateServiceBayStatus(
            @PathVariable Long id,
            @Valid @RequestBody ServiceBayStatusUpdateDto statusDto) {
        log.info("PATCH /api/v1/service-bays/{}/status - Updating service bay status", id);
        ServiceBayResponseDto response = serviceBayService.updateServiceBayStatus(id, statusDto);
        return ResponseEntity.ok(response);
    }
}
