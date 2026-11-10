package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.*;
import com.serviceflow.backend.entity.BookingStatus;
import com.serviceflow.backend.exception.InvalidStatusTransitionException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.service.ServiceBookingService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ServiceBookingController.
 * Tests REST API endpoints with mocked service layer.
 */
@ExtendWith(MockitoExtension.class)
class ServiceBookingControllerTest {

    @Mock
    private ServiceBookingService serviceBookingService;

    @InjectMocks
    private ServiceBookingController serviceBookingController;

    private ServiceBookingRequestDto testRequest;
    private ServiceBookingResponseDto testResponse;

    @BeforeEach
    void setUp() {
        BookingServiceItemRequestDto serviceRequest = BookingServiceItemRequestDto.builder()
                .serviceId(1L)
                .quantity(1)
                .build();

        testRequest = ServiceBookingRequestDto.builder()
                .customerId(1L)
                .vehicleId(1L)
                .bookingDateTime(LocalDateTime.now().plusDays(1))
                .customerNotes("Regular service")
                .services(List.of(serviceRequest))
                .build();

        CustomerSummaryDto customerDto = CustomerSummaryDto.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .email("john@example.com")
                .build();

        VehicleSummaryDto vehicleDto = VehicleSummaryDto.builder()
                .id(1L)
                .registrationNumber("ABC123")
                .make("Toyota")
                .model("Camry")
                .build();

        BookingServiceItemResponseDto serviceDto = BookingServiceItemResponseDto.builder()
                .id(1L)
                .serviceItemId(1L)
                .serviceCode("OC001")
                .serviceName("Oil Change")
                .unitPrice(BigDecimal.valueOf(50.00))
                .estimatedDurationMinutes(30)
                .quantity(1)
                .lineTotal(BigDecimal.valueOf(50.00))
                .build();

        testResponse = ServiceBookingResponseDto.builder()
                .id(1L)
                .bookingNumber("BK-20261110-00001")
                .customer(customerDto)
                .vehicle(vehicleDto)
                .bookingDateTime(LocalDateTime.now().plusDays(1))
                .customerNotes("Regular service")
                .bookingStatus(BookingStatus.REQUESTED)
                .estimatedTotalPrice(BigDecimal.valueOf(50.00))
                .estimatedTotalDurationMinutes(30)
                .services(List.of(serviceDto))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createBooking_Success() {
        // Arrange
        when(serviceBookingService.createBooking(any(ServiceBookingRequestDto.class)))
                .thenReturn(testResponse);

        // Act
        ResponseEntity<ServiceBookingResponseDto> response = 
                serviceBookingController.createBooking(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertEquals("BK-20261110-00001", response.getBody().getBookingNumber());
        assertEquals(BookingStatus.REQUESTED, response.getBody().getBookingStatus());

        verify(serviceBookingService, times(1)).createBooking(any(ServiceBookingRequestDto.class));
    }

    @Test
    void createBooking_CustomerNotFound() {
        // Arrange
        when(serviceBookingService.createBooking(any(ServiceBookingRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            serviceBookingController.createBooking(testRequest)
        );
    }

    @Test
    void getBookingById_Success() {
        // Arrange
        when(serviceBookingService.getBookingById(1L)).thenReturn(testResponse);

        // Act
        ResponseEntity<ServiceBookingResponseDto> response = 
                serviceBookingController.getBookingById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertEquals("BK-20261110-00001", response.getBody().getBookingNumber());

        verify(serviceBookingService, times(1)).getBookingById(1L);
    }

    @Test
    void getBookingById_NotFound() {
        // Arrange
        when(serviceBookingService.getBookingById(999L))
                .thenThrow(new ResourceNotFoundException("Booking not found"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            serviceBookingController.getBookingById(999L)
        );
    }

    @Test
    void getAllBookings_Success() {
        // Arrange
        Page<ServiceBookingResponseDto> bookingPage = new PageImpl<>(List.of(testResponse));
        when(serviceBookingService.getAllBookings(any(Pageable.class))).thenReturn(bookingPage);

        // Act
        ResponseEntity<Page<ServiceBookingResponseDto>> response = 
                serviceBookingController.getAllBookings(0, 20, "bookingDateTime", "DESC");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());

        verify(serviceBookingService, times(1)).getAllBookings(any(Pageable.class));
    }

    @Test
    void getBookingsByCustomerId_Success() {
        // Arrange
        Page<ServiceBookingResponseDto> bookingPage = new PageImpl<>(List.of(testResponse));
        when(serviceBookingService.getBookingsByCustomerId(anyLong(), any(Pageable.class)))
                .thenReturn(bookingPage);

        // Act
        ResponseEntity<Page<ServiceBookingResponseDto>> response = 
                serviceBookingController.getBookingsByCustomerId(1L, 0, 20, "bookingDateTime", "DESC");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());

        verify(serviceBookingService, times(1)).getBookingsByCustomerId(anyLong(), any(Pageable.class));
    }

    @Test
    void getBookingsByVehicleId_Success() {
        // Arrange
        Page<ServiceBookingResponseDto> bookingPage = new PageImpl<>(List.of(testResponse));
        when(serviceBookingService.getBookingsByVehicleId(anyLong(), any(Pageable.class)))
                .thenReturn(bookingPage);

        // Act
        ResponseEntity<Page<ServiceBookingResponseDto>> response = 
                serviceBookingController.getBookingsByVehicleId(1L, 0, 20, "bookingDateTime", "DESC");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());

        verify(serviceBookingService, times(1)).getBookingsByVehicleId(anyLong(), any(Pageable.class));
    }

    @Test
    void updateBookingStatus_Success() {
        // Arrange
        testResponse.setBookingStatus(BookingStatus.CONFIRMED);
        when(serviceBookingService.updateBookingStatus(1L, BookingStatus.CONFIRMED))
                .thenReturn(testResponse);

        Map<String, String> statusRequest = new HashMap<>();
        statusRequest.put("status", "CONFIRMED");

        // Act
        ResponseEntity<ServiceBookingResponseDto> response = 
                serviceBookingController.updateBookingStatus(1L, statusRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BookingStatus.CONFIRMED, response.getBody().getBookingStatus());

        verify(serviceBookingService, times(1)).updateBookingStatus(1L, BookingStatus.CONFIRMED);
    }

    @Test
    void updateBookingStatus_InvalidTransition() {
        // Arrange
        when(serviceBookingService.updateBookingStatus(1L, BookingStatus.CONFIRMED))
                .thenThrow(new InvalidStatusTransitionException("Invalid transition"));

        Map<String, String> statusRequest = new HashMap<>();
        statusRequest.put("status", "CONFIRMED");

        // Act & Assert
        assertThrows(InvalidStatusTransitionException.class, () -> 
            serviceBookingController.updateBookingStatus(1L, statusRequest)
        );
    }

    @Test
    void updateBookingStatus_MissingStatus() {
        // Arrange
        Map<String, String> statusRequest = new HashMap<>();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            serviceBookingController.updateBookingStatus(1L, statusRequest)
        );
    }

    @Test
    void updateBookingStatus_InvalidStatus() {
        // Arrange
        Map<String, String> statusRequest = new HashMap<>();
        statusRequest.put("status", "INVALID");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            serviceBookingController.updateBookingStatus(1L, statusRequest)
        );
    }

    @Test
    void cancelBooking_Success() {
        // Arrange
        doNothing().when(serviceBookingService).cancelBooking(1L);

        // Act
        ResponseEntity<Void> response = serviceBookingController.cancelBooking(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(serviceBookingService, times(1)).cancelBooking(1L);
    }

    @Test
    void cancelBooking_NotFound() {
        // Arrange
        doThrow(new ResourceNotFoundException("Booking not found"))
                .when(serviceBookingService).cancelBooking(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            serviceBookingController.cancelBooking(999L)
        );
    }

    @Test
    void getAllBookings_WithCustomPagination() {
        // Arrange
        Page<ServiceBookingResponseDto> bookingPage = new PageImpl<>(List.of(testResponse));
        when(serviceBookingService.getAllBookings(any(Pageable.class))).thenReturn(bookingPage);

        // Act
        ResponseEntity<Page<ServiceBookingResponseDto>> response = 
                serviceBookingController.getAllBookings(1, 10, "bookingDateTime", "ASC");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getTotalElements() >= 1);
    }
}
