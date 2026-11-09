package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.ServiceItemRequestDto;
import com.serviceflow.backend.dto.ServiceItemResponseDto;
import com.serviceflow.backend.entity.ServiceItem;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.repository.ServiceItemRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ServiceItemService.
 * Tests service catalogue business logic and operations.
 */
@ExtendWith(MockitoExtension.class)
class ServiceItemServiceTest {

    @Mock
    private ServiceItemRepository serviceItemRepository;

    @InjectMocks
    private ServiceItemService serviceItemService;

    private ServiceItem serviceItem;
    private ServiceItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        serviceItem = ServiceItem.builder()
                .id(1L)
                .serviceCode("OC001")
                .name("Engine Oil Change")
                .description("Complete engine oil change with filter replacement")
                .category("Maintenance")
                .basePrice(new BigDecimal("50.00"))
                .estimatedDurationMinutes(30)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        requestDto = ServiceItemRequestDto.builder()
                .serviceCode("oc001")
                .name("Engine Oil Change")
                .description("Complete engine oil change with filter replacement")
                .category("Maintenance")
                .basePrice(new BigDecimal("50.00"))
                .estimatedDurationMinutes(30)
                .active(true)
                .build();
    }

    @Test
    void createServiceItem_Success() {
        when(serviceItemRepository.existsByServiceCode("OC001")).thenReturn(false);
        when(serviceItemRepository.save(any(ServiceItem.class))).thenReturn(serviceItem);

        ServiceItemResponseDto result = serviceItemService.createServiceItem(requestDto);

        assertNotNull(result);
        assertEquals("OC001", result.getServiceCode());
        assertEquals("Engine Oil Change", result.getName());
        assertEquals(new BigDecimal("50.00"), result.getBasePrice());
        verify(serviceItemRepository, times(1)).save(any(ServiceItem.class));
    }

    @Test
    void createServiceItem_DuplicateCode() {
        when(serviceItemRepository.existsByServiceCode("OC001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                serviceItemService.createServiceItem(requestDto));

        verify(serviceItemRepository, never()).save(any(ServiceItem.class));
    }

    @Test
    void createServiceItem_CodeNormalization() {
        requestDto.setServiceCode(" oc001 ");
        when(serviceItemRepository.existsByServiceCode("OC001")).thenReturn(false);
        when(serviceItemRepository.save(any(ServiceItem.class))).thenReturn(serviceItem);

        ServiceItemResponseDto result = serviceItemService.createServiceItem(requestDto);

        assertNotNull(result);
        assertEquals("OC001", result.getServiceCode());
        verify(serviceItemRepository, times(1)).existsByServiceCode("OC001");
    }

    @Test
    void createServiceItem_DefaultActiveTrue() {
        requestDto.setActive(null);
        when(serviceItemRepository.existsByServiceCode("OC001")).thenReturn(false);
        when(serviceItemRepository.save(any(ServiceItem.class))).thenReturn(serviceItem);

        ServiceItemResponseDto result = serviceItemService.createServiceItem(requestDto);

        assertNotNull(result);
        assertTrue(result.getActive());
    }

    @Test
    void getServiceItemById_Success() {
        when(serviceItemRepository.findById(1L)).thenReturn(Optional.of(serviceItem));

        ServiceItemResponseDto result = serviceItemService.getServiceItemById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("OC001", result.getServiceCode());
    }

    @Test
    void getServiceItemById_NotFound() {
        when(serviceItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                serviceItemService.getServiceItemById(999L));
    }

    @Test
    void getAllServiceItems_Success() {
        Page<ServiceItem> page = new PageImpl<>(Arrays.asList(serviceItem));
        Pageable pageable = PageRequest.of(0, 20);

        when(serviceItemRepository.findAll(pageable)).thenReturn(page);

        Page<ServiceItemResponseDto> result = serviceItemService.getAllServiceItems(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("OC001", result.getContent().get(0).getServiceCode());
    }

    @Test
    void getActiveServiceItems_Success() {
        Page<ServiceItem> page = new PageImpl<>(Arrays.asList(serviceItem));
        Pageable pageable = PageRequest.of(0, 20);

        when(serviceItemRepository.findByActive(true, pageable)).thenReturn(page);

        Page<ServiceItemResponseDto> result = serviceItemService.getActiveServiceItems(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getActive());
    }

    @Test
    void getServiceItemsByCategory_Success() {
        Page<ServiceItem> page = new PageImpl<>(Arrays.asList(serviceItem));
        Pageable pageable = PageRequest.of(0, 20);

        when(serviceItemRepository.findByCategory("Maintenance", pageable)).thenReturn(page);

        Page<ServiceItemResponseDto> result = serviceItemService.getServiceItemsByCategory("Maintenance", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Maintenance", result.getContent().get(0).getCategory());
    }

    @Test
    void updateServiceItem_Success() {
        requestDto.setName("Premium Oil Change");
        requestDto.setBasePrice(new BigDecimal("75.00"));

        when(serviceItemRepository.findById(1L)).thenReturn(Optional.of(serviceItem));
        when(serviceItemRepository.save(any(ServiceItem.class))).thenReturn(serviceItem);

        ServiceItemResponseDto result = serviceItemService.updateServiceItem(1L, requestDto);

        assertNotNull(result);
        verify(serviceItemRepository, times(1)).save(any(ServiceItem.class));
    }

    @Test
    void updateServiceItem_NotFound() {
        when(serviceItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                serviceItemService.updateServiceItem(999L, requestDto));

        verify(serviceItemRepository, never()).save(any(ServiceItem.class));
    }

    @Test
    void updateServiceItem_DuplicateCodeChange() {
        requestDto.setServiceCode("OC002");

        when(serviceItemRepository.findById(1L)).thenReturn(Optional.of(serviceItem));
        when(serviceItemRepository.existsByServiceCode("OC002")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                serviceItemService.updateServiceItem(1L, requestDto));

        verify(serviceItemRepository, never()).save(any(ServiceItem.class));
    }

    @Test
    void updateServiceItem_SameCode() {
        when(serviceItemRepository.findById(1L)).thenReturn(Optional.of(serviceItem));
        when(serviceItemRepository.save(any(ServiceItem.class))).thenReturn(serviceItem);

        ServiceItemResponseDto result = serviceItemService.updateServiceItem(1L, requestDto);

        assertNotNull(result);
        verify(serviceItemRepository, times(1)).save(any(ServiceItem.class));
    }

    @Test
    void updateServiceItem_PreservesActiveWhenNotProvided() {
        requestDto.setActive(null);
        serviceItem.setActive(false);

        when(serviceItemRepository.findById(1L)).thenReturn(Optional.of(serviceItem));
        when(serviceItemRepository.save(any(ServiceItem.class))).thenReturn(serviceItem);

        ServiceItemResponseDto result = serviceItemService.updateServiceItem(1L, requestDto);

        assertNotNull(result);
        verify(serviceItemRepository, times(1)).save(any(ServiceItem.class));
    }

    @Test
    void updateServiceItemStatus_Success() {
        when(serviceItemRepository.findById(1L)).thenReturn(Optional.of(serviceItem));
        when(serviceItemRepository.save(any(ServiceItem.class))).thenReturn(serviceItem);

        ServiceItemResponseDto result = serviceItemService.updateServiceItemStatus(1L, false);

        assertNotNull(result);
        verify(serviceItemRepository, times(1)).save(any(ServiceItem.class));
    }

    @Test
    void updateServiceItemStatus_NotFound() {
        when(serviceItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                serviceItemService.updateServiceItemStatus(999L, false));

        verify(serviceItemRepository, never()).save(any(ServiceItem.class));
    }

    @Test
    void deactivateServiceItem_Success() {
        when(serviceItemRepository.findById(1L)).thenReturn(Optional.of(serviceItem));
        when(serviceItemRepository.save(any(ServiceItem.class))).thenReturn(serviceItem);

        assertDoesNotThrow(() -> serviceItemService.deactivateServiceItem(1L));

        verify(serviceItemRepository, times(1)).save(any(ServiceItem.class));
    }

    @Test
    void deactivateServiceItem_NotFound() {
        when(serviceItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                serviceItemService.deactivateServiceItem(999L));

        verify(serviceItemRepository, never()).save(any(ServiceItem.class));
    }

    @Test
    void deactivateServiceItem_SetsActiveToFalse() {
        when(serviceItemRepository.findById(1L)).thenReturn(Optional.of(serviceItem));
        when(serviceItemRepository.save(any(ServiceItem.class))).thenAnswer(invocation -> {
            ServiceItem saved = invocation.getArgument(0);
            assertFalse(saved.getActive());
            return saved;
        });

        serviceItemService.deactivateServiceItem(1L);

        verify(serviceItemRepository, times(1)).save(any(ServiceItem.class));
    }
}
