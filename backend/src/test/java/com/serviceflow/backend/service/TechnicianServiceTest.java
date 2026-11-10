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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TechnicianService.
 */
@ExtendWith(MockitoExtension.class)
class TechnicianServiceTest {

    @Mock
    private TechnicianRepository technicianRepository;

    @InjectMocks
    private TechnicianService technicianService;

    private TechnicianRequestDto requestDto;
    private Technician technician;

    @BeforeEach
    void setUp() {
        requestDto = TechnicianRequestDto.builder()
                .technicianCode("TECH001")
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@serviceflow.com")
                .phone("1234567890")
                .specialization(TechnicianSpecialization.ENGINE)
                .experienceYears(5)
                .status(TechnicianStatus.AVAILABLE)
                .active(true)
                .build();

        technician = Technician.builder()
                .id(1L)
                .technicianCode("TECH001")
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@serviceflow.com")
                .phone("1234567890")
                .specialization(TechnicianSpecialization.ENGINE)
                .experienceYears(5)
                .status(TechnicianStatus.AVAILABLE)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createTechnician_Success() {
        when(technicianRepository.existsByTechnicianCode(anyString())).thenReturn(false);
        when(technicianRepository.existsByEmail(anyString())).thenReturn(false);
        when(technicianRepository.save(any(Technician.class))).thenReturn(technician);

        TechnicianResponseDto response = technicianService.createTechnician(requestDto);

        assertNotNull(response);
        assertEquals("TECH001", response.getTechnicianCode());
        assertEquals("John", response.getFirstName());
        assertEquals(TechnicianSpecialization.ENGINE, response.getSpecialization());
        verify(technicianRepository).save(any(Technician.class));
    }

    @Test
    void createTechnician_DuplicateCode_ThrowsException() {
        when(technicianRepository.existsByTechnicianCode(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> technicianService.createTechnician(requestDto));
        verify(technicianRepository, never()).save(any(Technician.class));
    }

    @Test
    void createTechnician_DuplicateEmail_ThrowsException() {
        when(technicianRepository.existsByTechnicianCode(anyString())).thenReturn(false);
        when(technicianRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> technicianService.createTechnician(requestDto));
        verify(technicianRepository, never()).save(any(Technician.class));
    }

    @Test
    void getTechnicianById_Success() {
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(technician));

        TechnicianResponseDto response = technicianService.getTechnicianById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("TECH001", response.getTechnicianCode());
    }

    @Test
    void getTechnicianById_NotFound_ThrowsException() {
        when(technicianRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> technicianService.getTechnicianById(1L));
    }

    @Test
    void getAllTechnicians_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Technician> page = new PageImpl<>(List.of(technician));
        when(technicianRepository.findAll(pageable)).thenReturn(page);

        Page<TechnicianResponseDto> result = technicianService.getAllTechnicians(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("TECH001", result.getContent().get(0).getTechnicianCode());
    }

    @Test
    void getTechniciansByStatus_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Technician> page = new PageImpl<>(List.of(technician));
        when(technicianRepository.findByStatus(TechnicianStatus.AVAILABLE, pageable))
                .thenReturn(page);

        Page<TechnicianResponseDto> result = technicianService
                .getTechniciansByStatus(TechnicianStatus.AVAILABLE, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(TechnicianStatus.AVAILABLE, result.getContent().get(0).getStatus());
    }

    @Test
    void getTechniciansBySpecialization_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Technician> page = new PageImpl<>(List.of(technician));
        when(technicianRepository.findBySpecialization(TechnicianSpecialization.ENGINE, pageable))
                .thenReturn(page);

        Page<TechnicianResponseDto> result = technicianService
                .getTechniciansBySpecialization(TechnicianSpecialization.ENGINE, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(TechnicianSpecialization.ENGINE, result.getContent().get(0).getSpecialization());
    }

    @Test
    void getActiveTechnicians_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Technician> page = new PageImpl<>(List.of(technician));
        when(technicianRepository.findByActiveTrue(pageable)).thenReturn(page);

        Page<TechnicianResponseDto> result = technicianService.getActiveTechnicians(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getActive());
    }

    @Test
    void updateTechnician_Success() {
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(technician));
        when(technicianRepository.save(any(Technician.class))).thenReturn(technician);

        TechnicianResponseDto response = technicianService.updateTechnician(1L, requestDto);

        assertNotNull(response);
        verify(technicianRepository).save(any(Technician.class));
    }

    @Test
    void updateTechnician_NotFound_ThrowsException() {
        when(technicianRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> technicianService.updateTechnician(1L, requestDto));
    }

    @Test
    void updateTechnicianStatus_Success() {
        TechnicianStatusUpdateDto statusDto = TechnicianStatusUpdateDto.builder()
                .status(TechnicianStatus.ON_LEAVE)
                .active(false)
                .build();

        when(technicianRepository.findById(1L)).thenReturn(Optional.of(technician));
        when(technicianRepository.save(any(Technician.class))).thenReturn(technician);

        TechnicianResponseDto response = technicianService.updateTechnicianStatus(1L, statusDto);

        assertNotNull(response);
        verify(technicianRepository).save(any(Technician.class));
    }
}
