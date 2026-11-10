package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.ServiceBayRequestDto;
import com.serviceflow.backend.dto.ServiceBayResponseDto;
import com.serviceflow.backend.dto.ServiceBayStatusUpdateDto;
import com.serviceflow.backend.entity.BayType;
import com.serviceflow.backend.entity.ServiceBay;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.repository.ServiceBayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for service bay management operations.
 * Implements business logic, validation, and transaction management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceBayService {

    private final ServiceBayRepository serviceBayRepository;

    /**
     * Create a new service bay.
     * Validates uniqueness of bay code.
     */
    @Transactional
    public ServiceBayResponseDto createServiceBay(ServiceBayRequestDto requestDto) {
        log.info("Creating service bay with code: {}", requestDto.getBayCode());

        // Validate bay code uniqueness
        if (serviceBayRepository.existsByBayCode(requestDto.getBayCode())) {
            throw new DuplicateResourceException("Bay code", requestDto.getBayCode());
        }

        ServiceBay bay = ServiceBay.builder()
                .bayCode(requestDto.getBayCode().toUpperCase().trim())
                .bayName(requestDto.getBayName())
                .bayType(requestDto.getBayType())
                .locationDescription(requestDto.getLocationDescription())
                .active(requestDto.getActive() != null ? requestDto.getActive() : true)
                .build();

        ServiceBay savedBay = serviceBayRepository.save(bay);
        log.info("Service bay created successfully with id: {}", savedBay.getId());

        return mapToResponseDto(savedBay);
    }

    /**
     * Get a service bay by ID.
     */
    @Transactional(readOnly = true)
    public ServiceBayResponseDto getServiceBayById(Long id) {
        log.info("Fetching service bay with id: {}", id);
        ServiceBay bay = serviceBayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service Bay", id));
        return mapToResponseDto(bay);
    }

    /**
     * Get all service bays with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ServiceBayResponseDto> getAllServiceBays(Pageable pageable) {
        log.info("Fetching service bays with pagination: {}", pageable);
        return serviceBayRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }

    /**
     * Get service bays by type.
     */
    @Transactional(readOnly = true)
    public Page<ServiceBayResponseDto> getServiceBaysByType(BayType bayType, Pageable pageable) {
        log.info("Fetching service bays with type: {}", bayType);
        return serviceBayRepository.findByBayType(bayType, pageable)
                .map(this::mapToResponseDto);
    }

    /**
     * Get active service bays.
     */
    @Transactional(readOnly = true)
    public Page<ServiceBayResponseDto> getActiveServiceBays(Pageable pageable) {
        log.info("Fetching active service bays");
        return serviceBayRepository.findByActiveTrue(pageable)
                .map(this::mapToResponseDto);
    }

    /**
     * Update an existing service bay.
     * Validates uniqueness of bay code if changed.
     */
    @Transactional
    public ServiceBayResponseDto updateServiceBay(Long id, ServiceBayRequestDto requestDto) {
        log.info("Updating service bay with id: {}", id);

        ServiceBay bay = serviceBayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service Bay", id));

        // Validate bay code uniqueness if changed
        String normalizedCode = requestDto.getBayCode().toUpperCase().trim();
        if (!bay.getBayCode().equals(normalizedCode)) {
            if (serviceBayRepository.existsByBayCode(normalizedCode)) {
                throw new DuplicateResourceException("Bay code", normalizedCode);
            }
            bay.setBayCode(normalizedCode);
        }

        bay.setBayName(requestDto.getBayName());
        bay.setBayType(requestDto.getBayType());
        bay.setLocationDescription(requestDto.getLocationDescription());

        if (requestDto.getActive() != null) {
            bay.setActive(requestDto.getActive());
        }

        ServiceBay updatedBay = serviceBayRepository.save(bay);
        log.info("Service bay updated successfully with id: {}", updatedBay.getId());

        return mapToResponseDto(updatedBay);
    }

    /**
     * Update service bay active status.
     */
    @Transactional
    public ServiceBayResponseDto updateServiceBayStatus(Long id, ServiceBayStatusUpdateDto statusDto) {
        log.info("Updating service bay status for id: {}", id);

        ServiceBay bay = serviceBayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service Bay", id));

        bay.setActive(statusDto.getActive());

        ServiceBay updatedBay = serviceBayRepository.save(bay);
        log.info("Service bay status updated successfully");

        return mapToResponseDto(updatedBay);
    }

    /**
     * Map entity to response DTO.
     */
    private ServiceBayResponseDto mapToResponseDto(ServiceBay bay) {
        return ServiceBayResponseDto.builder()
                .id(bay.getId())
                .bayCode(bay.getBayCode())
                .bayName(bay.getBayName())
                .bayType(bay.getBayType())
                .locationDescription(bay.getLocationDescription())
                .active(bay.getActive())
                .createdAt(bay.getCreatedAt())
                .updatedAt(bay.getUpdatedAt())
                .build();
    }
}
