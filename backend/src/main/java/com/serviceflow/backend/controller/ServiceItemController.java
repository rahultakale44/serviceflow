package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.ServiceItemRequestDto;
import com.serviceflow.backend.dto.ServiceItemResponseDto;
import com.serviceflow.backend.service.ServiceItemService;
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

import java.util.Map;

/**
 * REST Controller for service catalogue management operations.
 * Provides CRUD endpoints for service items under /api/v1/services.
 */
@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Slf4j
public class ServiceItemController {

    private final ServiceItemService serviceItemService;

    /**
     * Create a new service item.
     *
     * @param requestDto service item data
     * @return created service item with 201 status
     */
    @PostMapping
    public ResponseEntity<ServiceItemResponseDto> createServiceItem(
            @Valid @RequestBody ServiceItemRequestDto requestDto) {
        log.info("POST /api/v1/services - Creating new service item");
        ServiceItemResponseDto response = serviceItemService.createServiceItem(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a service item by ID.
     *
     * @param id service item ID
     * @return service item data with 200 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceItemResponseDto> getServiceItemById(@PathVariable Long id) {
        log.info("GET /api/v1/services/{} - Fetching service item", id);
        ServiceItemResponseDto response = serviceItemService.getServiceItemById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all service items with pagination.
     *
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @param sortBy field to sort by (default "id")
     * @param direction sort direction (default "ASC")
     * @return paginated list of service items with 200 status
     */
    @GetMapping
    public ResponseEntity<Page<ServiceItemResponseDto>> getAllServiceItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("GET /api/v1/services - Fetching service items (page: {}, size: {})", page, size);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<ServiceItemResponseDto> response = serviceItemService.getAllServiceItems(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all active service items with pagination.
     *
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @param sortBy field to sort by (default "id")
     * @param direction sort direction (default "ASC")
     * @return paginated list of active service items with 200 status
     */
    @GetMapping("/active")
    public ResponseEntity<Page<ServiceItemResponseDto>> getActiveServiceItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("GET /api/v1/services/active - Fetching active service items (page: {}, size: {})", page, size);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<ServiceItemResponseDto> response = serviceItemService.getActiveServiceItems(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get service items by category with pagination.
     *
     * @param category service category
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @param sortBy field to sort by (default "id")
     * @param direction sort direction (default "ASC")
     * @return paginated list of service items in the category with 200 status
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ServiceItemResponseDto>> getServiceItemsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("GET /api/v1/services/category/{} - Fetching service items by category (page: {}, size: {})",
                category, page, size);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<ServiceItemResponseDto> response = serviceItemService.getServiceItemsByCategory(category, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing service item.
     *
     * @param id service item ID
     * @param requestDto updated service item data
     * @return updated service item with 200 status
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceItemResponseDto> updateServiceItem(
            @PathVariable Long id,
            @Valid @RequestBody ServiceItemRequestDto requestDto) {
        log.info("PUT /api/v1/services/{} - Updating service item", id);
        ServiceItemResponseDto response = serviceItemService.updateServiceItem(id, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Update the active status of a service item (activate/deactivate).
     *
     * @param id service item ID
     * @param statusRequest request body containing active status
     * @return updated service item with 200 status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceItemResponseDto> updateServiceItemStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> statusRequest) {
        log.info("PATCH /api/v1/services/{}/status - Updating service item status", id);

        Boolean active = statusRequest.get("active");
        if (active == null) {
            throw new IllegalArgumentException("Active status must be provided");
        }

        ServiceItemResponseDto response = serviceItemService.updateServiceItemStatus(id, active);
        return ResponseEntity.ok(response);
    }

    /**
     * Safely deactivate a service item (soft delete).
     *
     * @param id service item ID
     * @return 204 No Content status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateServiceItem(@PathVariable Long id) {
        log.info("DELETE /api/v1/services/{} - Deactivating service item", id);
        serviceItemService.deactivateServiceItem(id);
        return ResponseEntity.noContent().build();
    }
}
