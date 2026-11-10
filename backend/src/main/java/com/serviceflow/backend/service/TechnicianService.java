package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.TechnicianRequestDto;
import com.serviceflow.backend.dto.TechnicianResponseDto;
import com.serviceflow.backend.dto.TechnicianStatusUpdateDto;
import com.serviceflow.backend.entity.Technician;
import com.serviceflow.backend.entity.TechnicianSpecialization;
import com.serviceflow.backend.entity.TechnicianStatus;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for technician management operations.
 * Implements business logic, validation, and transaction management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TechnicianService {

    private final TechnicianRepository technicianRepository;

    /**
     * Create a new technician.
     * Validates uniqueness of technician code and email.
     */
    @Transactional
    public TechnicianResponseDto createTechnician(TechnicianRequestDto requestDto) {
        log.info("Creating technician with code: {}", requestDto.getTechnicianCode());

        // Validate technician code uniqueness
        if (technicianRepository.existsByTechnicianCode(requestDto.getTechnicianCode())) {
            throw new DuplicateResourceException("Technician code", requestDto.getTechnicianCode());
        }

        // Validate email uniqueness (only if email is provided and not blank)
        if (requestDto.getEmail() != null && !requestDto.getEmail().isBlank()) {
            if (technicianRepository.existsByEmail(requestDto.getEmail())) {
                throw new DuplicateResourceException("Email", requestDto.getEmail());
            }
        }

        Technician technician = Technician.builder()
                .technicianCode(requestDto.getTechnicianCode().toUpperCase().trim())
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .email(normalizeEmail(requestDto.getEmail()))
                .phone(requestDto.getPhone())
                .specialization(requestDto.getSpecialization())
                .experienceYears(requestDto.getExperienceYears())
                .status(requestDto.getStatus() != null ? requestDto.getStatus() : TechnicianStatus.AVAILABLE)
                .active(requestDto.getActive() != null ? requestDto.getActive() : true)
                .build();

        Technician savedTechnician = technicianRepository.save(technician);
        log.info("Technician created successfully with id: {}", savedTechnician.getId());

        return mapToResponseDto(savedTechnician);
    }

    /**
     * Get a technician by ID.
     */
    @Transactional(readOnly = true)
    public TechnicianResponseDto getTechnicianById(Long id) {
        log.info("Fetching technician with id: {}", id);
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician", id));
        return mapToResponseDto(technician);
    }

    /**
     * Get all technicians with pagination.
     */
    @Transactional(readOnly = true)
    public Page<TechnicianResponseDto> getAllTechnicians(Pageable pageable) {
        log.info("Fetching technicians with pagination: {}", pageable);
        return technicianRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }

    /**
     * Get technicians by status.
     */
    @Transactional(readOnly = true)
    public Page<TechnicianResponseDto> getTechniciansByStatus(TechnicianStatus status, Pageable pageable) {
        log.info("Fetching technicians with status: {}", status);
        return technicianRepository.findByStatus(status, pageable)
                .map(this::mapToResponseDto);
    }

    /**
     * Get technicians by specialization.
     */
    @Transactional(readOnly = true)
    public Page<TechnicianResponseDto> getTechniciansBySpecialization(
            TechnicianSpecialization specialization, Pageable pageable) {
        log.info("Fetching technicians with specialization: {}", specialization);
        return technicianRepository.findBySpecialization(specialization, pageable)
                .map(this::mapToResponseDto);
    }

    /**
     * Get active technicians.
     */
    @Transactional(readOnly = true)
    public Page<TechnicianResponseDto> getActiveTechnicians(Pageable pageable) {
        log.info("Fetching active technicians");
        return technicianRepository.findByActiveTrue(pageable)
                .map(this::mapToResponseDto);
    }

    /**
     * Update an existing technician.
     * Validates uniqueness of technician code and email if changed.
     */
    @Transactional
    public TechnicianResponseDto updateTechnician(Long id, TechnicianRequestDto requestDto) {
        log.info("Updating technician with id: {}", id);

        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician", id));

        // Validate technician code uniqueness if changed
        String normalizedCode = requestDto.getTechnicianCode().toUpperCase().trim();
        if (!technician.getTechnicianCode().equals(normalizedCode)) {
            if (technicianRepository.existsByTechnicianCode(normalizedCode)) {
                throw new DuplicateResourceException("Technician code", normalizedCode);
            }
            technician.setTechnicianCode(normalizedCode);
        }

        // Validate email uniqueness if changed
        String normalizedEmail = normalizeEmail(requestDto.getEmail());
        if (normalizedEmail != null && !normalizedEmail.equals(technician.getEmail())) {
            if (technicianRepository.existsByEmail(normalizedEmail)) {
                throw new DuplicateResourceException("Email", normalizedEmail);
            }
        }

        technician.setFirstName(requestDto.getFirstName());
        technician.setLastName(requestDto.getLastName());
        technician.setEmail(normalizedEmail);
        technician.setPhone(requestDto.getPhone());
        technician.setSpecialization(requestDto.getSpecialization());
        technician.setExperienceYears(requestDto.getExperienceYears());

        if (requestDto.getStatus() != null) {
            technician.setStatus(requestDto.getStatus());
        }
        if (requestDto.getActive() != null) {
            technician.setActive(requestDto.getActive());
        }

        Technician updatedTechnician = technicianRepository.save(technician);
        log.info("Technician updated successfully with id: {}", updatedTechnician.getId());

        return mapToResponseDto(updatedTechnician);
    }

    /**
     * Update technician status and active flag.
     */
    @Transactional
    public TechnicianResponseDto updateTechnicianStatus(Long id, TechnicianStatusUpdateDto statusDto) {
        log.info("Updating technician status for id: {}", id);

        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician", id));

        technician.setStatus(statusDto.getStatus());
        if (statusDto.getActive() != null) {
            technician.setActive(statusDto.getActive());
        }

        Technician updatedTechnician = technicianRepository.save(technician);
        log.info("Technician status updated successfully");

        return mapToResponseDto(updatedTechnician);
    }

    /**
     * Map entity to response DTO.
     */
    private TechnicianResponseDto mapToResponseDto(Technician technician) {
        return TechnicianResponseDto.builder()
                .id(technician.getId())
                .technicianCode(technician.getTechnicianCode())
                .firstName(technician.getFirstName())
                .lastName(technician.getLastName())
                .email(technician.getEmail())
                .phone(technician.getPhone())
                .specialization(technician.getSpecialization())
                .experienceYears(technician.getExperienceYears())
                .status(technician.getStatus())
                .active(technician.getActive())
                .createdAt(technician.getCreatedAt())
                .updatedAt(technician.getUpdatedAt())
                .build();
    }

    /**
     * Normalize email address (convert to lowercase and trim).
     * Returns null for null or blank input.
     */
    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.toLowerCase().trim();
    }
}
