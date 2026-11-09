package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.ServiceItemRequestDto;
import com.serviceflow.backend.dto.ServiceItemResponseDto;
import com.serviceflow.backend.entity.ServiceItem;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for managing service catalogue items.
 * Handles business logic for service item operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceItemService {

    private final ServiceItemRepository serviceItemRepository;

    /**
     * Create a new service item in the catalogue.
     *
     * @param requestDto service item data
     * @return created service item
     * @throws DuplicateResourceException if service code already exists
     */
    @Transactional
    public ServiceItemResponseDto createServiceItem(ServiceItemRequestDto requestDto) {
        String normalizedCode = normalizeServiceCode(requestDto.getServiceCode());
        log.info("Creating service item with code: {}", normalizedCode);

        if (serviceItemRepository.existsByServiceCode(normalizedCode)) {
            log.warn("Service code already exists: {}", normalizedCode);
            throw new DuplicateResourceException("Service with code " + normalizedCode + " already exists");
        }

        ServiceItem serviceItem = ServiceItem.builder()
                .serviceCode(normalizedCode)
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .category(requestDto.getCategory())
                .basePrice(requestDto.getBasePrice())
                .estimatedDurationMinutes(requestDto.getEstimatedDurationMinutes())
                .active(requestDto.getActive() != null ? requestDto.getActive() : true)
                .build();

        ServiceItem savedServiceItem = serviceItemRepository.save(serviceItem);
        log.info("Service item created successfully with id: {}", savedServiceItem.getId());

        return mapToResponseDto(savedServiceItem);
    }

    /**
     * Get a service item by ID.
     *
     * @param id service item ID
     * @return service item data
     * @throws ResourceNotFoundException if service item not found
     */
    @Transactional(readOnly = true)
    public ServiceItemResponseDto getServiceItemById(Long id) {
        log.info("Fetching service item with id: {}", id);

        ServiceItem serviceItem = serviceItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service item not found with id: " + id));

        return mapToResponseDto(serviceItem);
    }

    /**
     * Get all service items with pagination.
     *
     * @param pageable pagination information
     * @return page of service items
     */
    @Transactional(readOnly = true)
    public Page<ServiceItemResponseDto> getAllServiceItems(Pageable pageable) {
        log.info("Fetching service items with pagination: {}", pageable);

        Page<ServiceItem> serviceItems = serviceItemRepository.findAll(pageable);
        return serviceItems.map(this::mapToResponseDto);
    }

    /**
     * Get all active service items with pagination.
     *
     * @param pageable pagination information
     * @return page of active service items
     */
    @Transactional(readOnly = true)
    public Page<ServiceItemResponseDto> getActiveServiceItems(Pageable pageable) {
        log.info("Fetching active service items with pagination: {}", pageable);

        Page<ServiceItem> serviceItems = serviceItemRepository.findByActive(true, pageable);
        return serviceItems.map(this::mapToResponseDto);
    }

    /**
     * Get service items by category with pagination.
     *
     * @param category service category
     * @param pageable pagination information
     * @return page of service items in the category
     */
    @Transactional(readOnly = true)
    public Page<ServiceItemResponseDto> getServiceItemsByCategory(String category, Pageable pageable) {
        log.info("Fetching service items for category: {} with pagination: {}", category, pageable);

        Page<ServiceItem> serviceItems = serviceItemRepository.findByCategory(category, pageable);
        return serviceItems.map(this::mapToResponseDto);
    }

    /**
     * Update an existing service item.
     *
     * @param id service item ID
     * @param requestDto updated service item data
     * @return updated service item
     * @throws ResourceNotFoundException if service item not found
     * @throws DuplicateResourceException if service code is changed to an existing code
     */
    @Transactional
    public ServiceItemResponseDto updateServiceItem(Long id, ServiceItemRequestDto requestDto) {
        log.info("Updating service item with id: {}", id);

        ServiceItem existingServiceItem = serviceItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service item not found with id: " + id));

        String normalizedCode = normalizeServiceCode(requestDto.getServiceCode());

        // Check if service code is being changed to a different existing code
        if (!existingServiceItem.getServiceCode().equals(normalizedCode) &&
                serviceItemRepository.existsByServiceCode(normalizedCode)) {
            log.warn("Service code already exists: {}", normalizedCode);
            throw new DuplicateResourceException("Service with code " + normalizedCode + " already exists");
        }

        existingServiceItem.setServiceCode(normalizedCode);
        existingServiceItem.setName(requestDto.getName());
        existingServiceItem.setDescription(requestDto.getDescription());
        existingServiceItem.setCategory(requestDto.getCategory());
        existingServiceItem.setBasePrice(requestDto.getBasePrice());
        existingServiceItem.setEstimatedDurationMinutes(requestDto.getEstimatedDurationMinutes());

        // Only update active status if explicitly provided
        if (requestDto.getActive() != null) {
            existingServiceItem.setActive(requestDto.getActive());
        }

        ServiceItem updatedServiceItem = serviceItemRepository.save(existingServiceItem);
        log.info("Service item updated successfully with id: {}", updatedServiceItem.getId());

        return mapToResponseDto(updatedServiceItem);
    }

    /**
     * Update the active status of a service item.
     *
     * @param id service item ID
     * @param active new active status
     * @return updated service item
     * @throws ResourceNotFoundException if service item not found
     */
    @Transactional
    public ServiceItemResponseDto updateServiceItemStatus(Long id, Boolean active) {
        log.info("Updating service item status with id: {} to active: {}", id, active);

        ServiceItem serviceItem = serviceItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service item not found with id: " + id));

        serviceItem.setActive(active);
        ServiceItem updatedServiceItem = serviceItemRepository.save(serviceItem);

        log.info("Service item status updated successfully with id: {}", updatedServiceItem.getId());

        return mapToResponseDto(updatedServiceItem);
    }

    /**
     * Safely deactivate a service item (soft delete).
     * Sets active to false instead of permanently deleting the record.
     *
     * @param id service item ID
     * @throws ResourceNotFoundException if service item not found
     */
    @Transactional
    public void deactivateServiceItem(Long id) {
        log.info("Deactivating service item with id: {}", id);

        ServiceItem serviceItem = serviceItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service item not found with id: " + id));

        serviceItem.setActive(false);
        serviceItemRepository.save(serviceItem);

        log.info("Service item deactivated successfully with id: {}", id);
    }

    /**
     * Normalize service code by trimming and converting to uppercase.
     *
     * @param serviceCode the service code to normalize
     * @return normalized service code
     */
    private String normalizeServiceCode(String serviceCode) {
        if (serviceCode == null) {
            return null;
        }
        return serviceCode.trim().toUpperCase();
    }

    /**
     * Map ServiceItem entity to ServiceItemResponseDto.
     *
     * @param serviceItem the service item entity
     * @return response DTO
     */
    private ServiceItemResponseDto mapToResponseDto(ServiceItem serviceItem) {
        return ServiceItemResponseDto.builder()
                .id(serviceItem.getId())
                .serviceCode(serviceItem.getServiceCode())
                .name(serviceItem.getName())
                .description(serviceItem.getDescription())
                .category(serviceItem.getCategory())
                .basePrice(serviceItem.getBasePrice())
                .estimatedDurationMinutes(serviceItem.getEstimatedDurationMinutes())
                .active(serviceItem.getActive())
                .createdAt(serviceItem.getCreatedAt())
                .updatedAt(serviceItem.getUpdatedAt())
                .build();
    }
}
