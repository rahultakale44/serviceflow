package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.ServiceBayRequestDto;
import com.serviceflow.backend.dto.ServiceBayResponseDto;
import com.serviceflow.backend.dto.ServiceBayStatusUpdateDto;
import com.serviceflow.backend.entity.BayType;
import com.serviceflow.backend.entity.ServiceBay;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.repository.ServiceBayRepository;
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
 * Unit tests for ServiceBayService.
 */
@ExtendWith(MockitoExtension.class)
class ServiceBayServiceTest {

    @Mock
    private ServiceBayRepository serviceBayRepository;

    @InjectMocks
    private ServiceBayService serviceBayService;

    private ServiceBayRequestDto requestDto;
    private ServiceBay serviceBay;

    @BeforeEach
    void setUp() {
        requestDto = ServiceBayRequestDto.builder()
                .bayCode("BAY01")
                .bayName("Engine Service Bay 1")
                .bayType(BayType.ENGINE)
                .locationDescription("Ground floor, left section")
                .active(true)
                .build();

        serviceBay = ServiceBay.builder()
                .id(1L)
                .bayCode("BAY01")
                .bayName("Engine Service Bay 1")
                .bayType(BayType.ENGINE)
                .locationDescription("Ground floor, left section")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createServiceBay_Success() {
        when(serviceBayRepository.existsByBayCode(anyString())).thenReturn(false);
        when(serviceBayRepository.save(any(ServiceBay.class))).thenReturn(serviceBay);

        ServiceBayResponseDto response = serviceBayService.createServiceBay(requestDto);

        assertNotNull(response);
        assertEquals("BAY01", response.getBayCode());
        assertEquals("Engine Service Bay 1", response.getBayName());
        assertEquals(BayType.ENGINE, response.getBayType());
        verify(serviceBayRepository).save(any(ServiceBay.class));
    }

    @Test
    void createServiceBay_DuplicateCode_ThrowsException() {
        when(serviceBayRepository.existsByBayCode(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> serviceBayService.createServiceBay(requestDto));
        verify(serviceBayRepository, never()).save(any(ServiceBay.class));
    }

    @Test
    void getServiceBayById_Success() {
        when(serviceBayRepository.findById(1L)).thenReturn(Optional.of(serviceBay));

        ServiceBayResponseDto response = serviceBayService.getServiceBayById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("BAY01", response.getBayCode());
    }

    @Test
    void getServiceBayById_NotFound_ThrowsException() {
        when(serviceBayRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> serviceBayService.getServiceBayById(1L));
    }

    @Test
    void getAllServiceBays_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceBay> page = new PageImpl<>(List.of(serviceBay));
        when(serviceBayRepository.findAll(pageable)).thenReturn(page);

        Page<ServiceBayResponseDto> result = serviceBayService.getAllServiceBays(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("BAY01", result.getContent().get(0).getBayCode());
    }

    @Test
    void getServiceBaysByType_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceBay> page = new PageImpl<>(List.of(serviceBay));
        when(serviceBayRepository.findByBayType(BayType.ENGINE, pageable)).thenReturn(page);

        Page<ServiceBayResponseDto> result = serviceBayService
                .getServiceBaysByType(BayType.ENGINE, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(BayType.ENGINE, result.getContent().get(0).getBayType());
    }

    @Test
    void getActiveServiceBays_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceBay> page = new PageImpl<>(List.of(serviceBay));
        when(serviceBayRepository.findByActiveTrue(pageable)).thenReturn(page);

        Page<ServiceBayResponseDto> result = serviceBayService.getActiveServiceBays(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getActive());
    }

    @Test
    void updateServiceBay_Success() {
        when(serviceBayRepository.findById(1L)).thenReturn(Optional.of(serviceBay));
        when(serviceBayRepository.save(any(ServiceBay.class))).thenReturn(serviceBay);

        ServiceBayResponseDto response = serviceBayService.updateServiceBay(1L, requestDto);

        assertNotNull(response);
        verify(serviceBayRepository).save(any(ServiceBay.class));
    }

    @Test
    void updateServiceBay_NotFound_ThrowsException() {
        when(serviceBayRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> serviceBayService.updateServiceBay(1L, requestDto));
    }

    @Test
    void updateServiceBayStatus_Success() {
        ServiceBayStatusUpdateDto statusDto = ServiceBayStatusUpdateDto.builder()
                .active(false)
                .build();

        when(serviceBayRepository.findById(1L)).thenReturn(Optional.of(serviceBay));
        when(serviceBayRepository.save(any(ServiceBay.class))).thenReturn(serviceBay);

        ServiceBayResponseDto response = serviceBayService.updateServiceBayStatus(1L, statusDto);

        assertNotNull(response);
        verify(serviceBayRepository).save(any(ServiceBay.class));
    }
}
