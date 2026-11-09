package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.ServiceItemRequestDto;
import com.serviceflow.backend.dto.ServiceItemResponseDto;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.service.ServiceItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ServiceItemController.
 * Tests REST API endpoints for service catalogue operations.
 */
@ExtendWith(MockitoExtension.class)
class ServiceItemControllerTest {

    @Mock
    private ServiceItemService serviceItemService;

    @InjectMocks
    private ServiceItemController serviceItemController;

    private ServiceItemRequestDto requestDto;
    private ServiceItemResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = ServiceItemRequestDto.builder()
                .serviceCode("OC001")
                .name("Engine Oil Change")
                .description("Complete engine oil change with filter replacement")
                .category("Maintenance")
                .basePrice(new BigDecimal("50.00"))
                .estimatedDurationMinutes(30)
                .active(true)
                .build();

        responseDto = ServiceItemResponseDto.builder()
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
    }

    @Test
    void createServiceItem_Success() {
        when(serviceItemService.createServiceItem(any(ServiceItemRequestDto.class))).thenReturn(responseDto);

        ResponseEntity<ServiceItemResponseDto> response = serviceItemController.createServiceItem(requestDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OC001", response.getBody().getServiceCode());
        assertEquals("Engine Oil Change", response.getBody().getName());
        verify(serviceItemService, times(1)).createServiceItem(any(ServiceItemRequestDto.class));
    }

    @Test
    void createServiceItem_DuplicateCode() {
        when(serviceItemService.createServiceItem(any(ServiceItemRequestDto.class)))
                .thenThrow(new DuplicateResourceException("Service with code OC001 already exists"));

        assertThrows(DuplicateResourceException.class, () ->
                serviceItemController.createServiceItem(requestDto));

        verify(serviceItemService, times(1)).createServiceItem(any(ServiceItemRequestDto.class));
    }

    @Test
    void getServiceItemById_Success() {
        when(serviceItemService.getServiceItemById(1L)).thenReturn(responseDto);

        ResponseEntity<ServiceItemResponseDto> response = serviceItemController.getServiceItemById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(serviceItemService, times(1)).getServiceItemById(1L);
    }

    @Test
    void getServiceItemById_NotFound() {
        when(serviceItemService.getServiceItemById(999L))
                .thenThrow(new ResourceNotFoundException("Service item not found with id: 999"));

        assertThrows(ResourceNotFoundException.class, () ->
                serviceItemController.getServiceItemById(999L));

        verify(serviceItemService, times(1)).getServiceItemById(999L);
    }

    @Test
    void getAllServiceItems_Success() {
        Page<ServiceItemResponseDto> page = new PageImpl<>(Arrays.asList(responseDto));

        when(serviceItemService.getAllServiceItems(any(PageRequest.class))).thenReturn(page);

        ResponseEntity<Page<ServiceItemResponseDto>> response = serviceItemController.getAllServiceItems(0, 20, "id", "ASC");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(serviceItemService, times(1)).getAllServiceItems(any(PageRequest.class));
    }

    @Test
    void getActiveServiceItems_Success() {
        Page<ServiceItemResponseDto> page = new PageImpl<>(Arrays.asList(responseDto));

        when(serviceItemService.getActiveServiceItems(any(PageRequest.class))).thenReturn(page);

        ResponseEntity<Page<ServiceItemResponseDto>> response = serviceItemController.getActiveServiceItems(0, 20, "id", "ASC");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertTrue(response.getBody().getContent().get(0).getActive());
        verify(serviceItemService, times(1)).getActiveServiceItems(any(PageRequest.class));
    }

    @Test
    void getServiceItemsByCategory_Success() {
        Page<ServiceItemResponseDto> page = new PageImpl<>(Arrays.asList(responseDto));

        when(serviceItemService.getServiceItemsByCategory(eq("Maintenance"), any(PageRequest.class)))
                .thenReturn(page);

        ResponseEntity<Page<ServiceItemResponseDto>> response = serviceItemController.getServiceItemsByCategory("Maintenance", 0, 20, "id", "ASC");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("Maintenance", response.getBody().getContent().get(0).getCategory());
        verify(serviceItemService, times(1)).getServiceItemsByCategory(eq("Maintenance"), any(PageRequest.class));
    }

    @Test
    void updateServiceItem_Success() {
        when(serviceItemService.updateServiceItem(eq(1L), any(ServiceItemRequestDto.class)))
                .thenReturn(responseDto);

        ResponseEntity<ServiceItemResponseDto> response = serviceItemController.updateServiceItem(1L, requestDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(serviceItemService, times(1)).updateServiceItem(eq(1L), any(ServiceItemRequestDto.class));
    }

    @Test
    void updateServiceItem_NotFound() {
        when(serviceItemService.updateServiceItem(eq(999L), any(ServiceItemRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Service item not found with id: 999"));

        assertThrows(ResourceNotFoundException.class, () ->
                serviceItemController.updateServiceItem(999L, requestDto));

        verify(serviceItemService, times(1)).updateServiceItem(eq(999L), any(ServiceItemRequestDto.class));
    }

    @Test
    void updateServiceItemStatus_Success() {
        Map<String, Boolean> statusRequest = new HashMap<>();
        statusRequest.put("active", false);

        responseDto.setActive(false);
        when(serviceItemService.updateServiceItemStatus(1L, false)).thenReturn(responseDto);

        ResponseEntity<ServiceItemResponseDto> response = serviceItemController.updateServiceItemStatus(1L, statusRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getActive());
        verify(serviceItemService, times(1)).updateServiceItemStatus(1L, false);
    }

    @Test
    void updateServiceItemStatus_NotFound() {
        Map<String, Boolean> statusRequest = new HashMap<>();
        statusRequest.put("active", false);

        when(serviceItemService.updateServiceItemStatus(999L, false))
                .thenThrow(new ResourceNotFoundException("Service item not found with id: 999"));

        assertThrows(ResourceNotFoundException.class, () ->
                serviceItemController.updateServiceItemStatus(999L, statusRequest));

        verify(serviceItemService, times(1)).updateServiceItemStatus(999L, false);
    }

    @Test
    void deactivateServiceItem_Success() {
        doNothing().when(serviceItemService).deactivateServiceItem(1L);

        ResponseEntity<Void> response = serviceItemController.deactivateServiceItem(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(serviceItemService, times(1)).deactivateServiceItem(1L);
    }

    @Test
    void deactivateServiceItem_NotFound() {
        doThrow(new ResourceNotFoundException("Service item not found with id: 999"))
                .when(serviceItemService).deactivateServiceItem(999L);

        assertThrows(ResourceNotFoundException.class, () ->
                serviceItemController.deactivateServiceItem(999L));

        verify(serviceItemService, times(1)).deactivateServiceItem(999L);
    }
}
