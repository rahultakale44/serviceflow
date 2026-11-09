package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.VehicleRequestDto;
import com.serviceflow.backend.dto.VehicleResponseDto;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VehicleController.
 * Tests the REST API endpoints with mocked service layer.
 */
@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    private VehicleRequestDto requestDto;
    private VehicleResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = VehicleRequestDto.builder()
                .registrationNumber("ABC123")
                .make("Toyota")
                .model("Camry")
                .manufacturingYear(2020)
                .fuelType("Petrol")
                .vin("1HGBH41JXMN109186")
                .customerId(1L)
                .build();

        VehicleResponseDto.CustomerSummaryDto customerSummary = VehicleResponseDto.CustomerSummaryDto.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .build();

        responseDto = VehicleResponseDto.builder()
                .id(1L)
                .registrationNumber("ABC123")
                .make("Toyota")
                .model("Camry")
                .manufacturingYear(2020)
                .fuelType("Petrol")
                .vin("1HGBH41JXMN109186")
                .customer(customerSummary)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createVehicle_ValidRequest_ReturnsCreated() {
        // Arrange
        when(vehicleService.createVehicle(any(VehicleRequestDto.class))).thenReturn(responseDto);

        // Act
        ResponseEntity<VehicleResponseDto> response = vehicleController.createVehicle(requestDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        assertEquals("ABC123", response.getBody().getRegistrationNumber());
        verify(vehicleService).createVehicle(any(VehicleRequestDto.class));
    }

    @Test
    void createVehicle_CustomerNotFound_ThrowsException() {
        // Arrange
        when(vehicleService.createVehicle(any(VehicleRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Customer", 1L));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> vehicleController.createVehicle(requestDto));
    }

    @Test
    void createVehicle_DuplicateRegistration_ThrowsException() {
        // Arrange
        when(vehicleService.createVehicle(any(VehicleRequestDto.class)))
                .thenThrow(new DuplicateResourceException("Registration number", "ABC123"));

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> vehicleController.createVehicle(requestDto));
    }

    @Test
    void getVehicleById_ExistingVehicle_ReturnsOk() {
        // Arrange
        when(vehicleService.getVehicleById(1L)).thenReturn(responseDto);

        // Act
        ResponseEntity<VehicleResponseDto> response = vehicleController.getVehicleById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(vehicleService).getVehicleById(1L);
    }

    @Test
    void getVehicleById_NonExistingVehicle_ThrowsException() {
        // Arrange
        when(vehicleService.getVehicleById(999L))
                .thenThrow(new ResourceNotFoundException("Vehicle", 999L));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> vehicleController.getVehicleById(999L));
    }

    @Test
    void getAllVehicles_ReturnsOk() {
        // Arrange
        Page<VehicleResponseDto> page = new PageImpl<>(List.of(responseDto));
        when(vehicleService.getAllVehicles(any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<VehicleResponseDto>> response = vehicleController.getAllVehicles(0, 20, "id", "ASC");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("ABC123", response.getBody().getContent().get(0).getRegistrationNumber());
        verify(vehicleService).getAllVehicles(any(Pageable.class));
    }

    @Test
    void getVehiclesByCustomerId_ReturnsOk() {
        // Arrange
        Page<VehicleResponseDto> page = new PageImpl<>(List.of(responseDto));
        when(vehicleService.getVehiclesByCustomerId(eq(1L), any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<VehicleResponseDto>> response = vehicleController.getVehiclesByCustomerId(1L, 0, 20, "id", "ASC");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(vehicleService).getVehiclesByCustomerId(eq(1L), any(Pageable.class));
    }

    @Test
    void getVehiclesByCustomerId_CustomerNotFound_ThrowsException() {
        // Arrange
        when(vehicleService.getVehiclesByCustomerId(eq(999L), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("Customer", 999L));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> vehicleController.getVehiclesByCustomerId(999L, 0, 20, "id", "ASC"));
    }

    @Test
    void updateVehicle_ValidRequest_ReturnsOk() {
        // Arrange
        VehicleRequestDto updateDto = VehicleRequestDto.builder()
                .registrationNumber("ABC123")
                .make("Honda")
                .model("Accord")
                .manufacturingYear(2021)
                .fuelType("Diesel")
                .vin("1HGBH41JXMN109186")
                .customerId(1L)
                .build();

        VehicleResponseDto updatedResponse = VehicleResponseDto.builder()
                .id(1L)
                .registrationNumber("ABC123")
                .make("Honda")
                .model("Accord")
                .manufacturingYear(2021)
                .fuelType("Diesel")
                .vin("1HGBH41JXMN109186")
                .customer(responseDto.getCustomer())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(vehicleService.updateVehicle(eq(1L), any(VehicleRequestDto.class)))
                .thenReturn(updatedResponse);

        // Act
        ResponseEntity<VehicleResponseDto> response = vehicleController.updateVehicle(1L, updateDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Honda", response.getBody().getMake());
        assertEquals("Accord", response.getBody().getModel());
        verify(vehicleService).updateVehicle(eq(1L), any(VehicleRequestDto.class));
    }

    @Test
    void updateVehicle_NonExistingVehicle_ThrowsException() {
        // Arrange
        when(vehicleService.updateVehicle(eq(999L), any(VehicleRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Vehicle", 999L));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> vehicleController.updateVehicle(999L, requestDto));
    }

    @Test
    void updateVehicle_OwnershipChange_ThrowsException() {
        // Arrange
        requestDto.setCustomerId(2L);
        when(vehicleService.updateVehicle(eq(1L), any(VehicleRequestDto.class)))
                .thenThrow(new DuplicateResourceException("Cannot change vehicle ownership"));

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> vehicleController.updateVehicle(1L, requestDto));
    }

    @Test
    void deleteVehicle_ExistingVehicle_ReturnsNoContent() {
        // Arrange
        doNothing().when(vehicleService).deleteVehicle(1L);

        // Act
        ResponseEntity<Void> response = vehicleController.deleteVehicle(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(vehicleService).deleteVehicle(1L);
    }

    @Test
    void deleteVehicle_NonExistingVehicle_ThrowsException() {
        // Arrange
        doThrow(new ResourceNotFoundException("Vehicle", 999L))
                .when(vehicleService).deleteVehicle(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> vehicleController.deleteVehicle(999L));
    }
}
