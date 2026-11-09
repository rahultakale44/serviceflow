package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.VehicleRequestDto;
import com.serviceflow.backend.dto.VehicleResponseDto;
import com.serviceflow.backend.entity.Customer;
import com.serviceflow.backend.entity.Vehicle;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.repository.CustomerRepository;
import com.serviceflow.backend.repository.VehicleRepository;
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
 * Unit tests for VehicleService.
 * Uses Mockito to mock repository dependencies.
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Customer customer;
    private VehicleRequestDto requestDto;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .email("john@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        requestDto = VehicleRequestDto.builder()
                .registrationNumber("ABC123")
                .make("Toyota")
                .model("Camry")
                .manufacturingYear(2020)
                .fuelType("Petrol")
                .vin("1HGBH41JXMN109186")
                .customerId(1L)
                .build();

        vehicle = Vehicle.builder()
                .id(1L)
                .registrationNumber("ABC123")
                .make("Toyota")
                .model("Camry")
                .manufacturingYear(2020)
                .fuelType("Petrol")
                .vin("1HGBH41JXMN109186")
                .customer(customer)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createVehicle_Success() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByRegistrationNumber(anyString())).thenReturn(false);
        when(vehicleRepository.existsByVin(anyString())).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // Act
        VehicleResponseDto response = vehicleService.createVehicle(requestDto);

        // Assert
        assertNotNull(response);
        assertEquals("ABC123", response.getRegistrationNumber());
        assertEquals("Toyota", response.getMake());
        assertEquals("Camry", response.getModel());
        assertNotNull(response.getCustomer());
        assertEquals(1L, response.getCustomer().getId());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_CustomerNotFound_ThrowsException() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> vehicleService.createVehicle(requestDto));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_DuplicateRegistration_ThrowsException() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByRegistrationNumber(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> vehicleService.createVehicle(requestDto));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_DuplicateVin_ThrowsException() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByRegistrationNumber(anyString())).thenReturn(false);
        when(vehicleRepository.existsByVin(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> vehicleService.createVehicle(requestDto));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_WithoutVin_Success() {
        // Arrange
        requestDto.setVin(null);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByRegistrationNumber(anyString())).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // Act
        VehicleResponseDto response = vehicleService.createVehicle(requestDto);

        // Assert
        assertNotNull(response);
        verify(vehicleRepository, never()).existsByVin(anyString());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_WithBlankVin_Success() {
        // Arrange
        requestDto.setVin("   ");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByRegistrationNumber(anyString())).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // Act
        VehicleResponseDto response = vehicleService.createVehicle(requestDto);

        // Assert
        assertNotNull(response);
        verify(vehicleRepository, never()).existsByVin(anyString());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_NormalizesRegistrationNumber() {
        // Arrange
        requestDto.setRegistrationNumber("abc123");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByRegistrationNumber("ABC123")).thenReturn(false);
        when(vehicleRepository.existsByVin(anyString())).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // Act
        VehicleResponseDto response = vehicleService.createVehicle(requestDto);

        // Assert
        assertNotNull(response);
        verify(vehicleRepository).existsByRegistrationNumber("ABC123");
    }

    @Test
    void createVehicle_NormalizesVin() {
        // Arrange
        requestDto.setVin("1hgbh41jxmn109186");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByRegistrationNumber(anyString())).thenReturn(false);
        when(vehicleRepository.existsByVin("1HGBH41JXMN109186")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // Act
        VehicleResponseDto response = vehicleService.createVehicle(requestDto);

        // Assert
        assertNotNull(response);
        verify(vehicleRepository).existsByVin("1HGBH41JXMN109186");
    }

    @Test
    void getVehicleById_Success() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        // Act
        VehicleResponseDto response = vehicleService.getVehicleById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ABC123", response.getRegistrationNumber());
        verify(vehicleRepository).findById(1L);
    }

    @Test
    void getVehicleById_NotFound_ThrowsException() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> vehicleService.getVehicleById(1L));
    }

    @Test
    void getAllVehicles_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Vehicle> vehiclePage = new PageImpl<>(List.of(vehicle));
        when(vehicleRepository.findAll(pageable)).thenReturn(vehiclePage);

        // Act
        Page<VehicleResponseDto> response = vehicleService.getAllVehicles(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("ABC123", response.getContent().get(0).getRegistrationNumber());
        verify(vehicleRepository).findAll(pageable);
    }

    @Test
    void getVehiclesByCustomerId_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Vehicle> vehiclePage = new PageImpl<>(List.of(vehicle));
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(vehicleRepository.findByCustomerId(1L, pageable)).thenReturn(vehiclePage);

        // Act
        Page<VehicleResponseDto> response = vehicleService.getVehiclesByCustomerId(1L, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(vehicleRepository).findByCustomerId(1L, pageable);
    }

    @Test
    void getVehiclesByCustomerId_CustomerNotFound_ThrowsException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        when(customerRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> vehicleService.getVehiclesByCustomerId(1L, pageable));
        verify(vehicleRepository, never()).findByCustomerId(any(), any());
    }

    @Test
    void updateVehicle_Success() {
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

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // Act
        VehicleResponseDto response = vehicleService.updateVehicle(1L, updateDto);

        // Assert
        assertNotNull(response);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void updateVehicle_NotFound_ThrowsException() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> vehicleService.updateVehicle(1L, requestDto));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void updateVehicle_OwnershipChange_ThrowsException() {
        // Arrange
        requestDto.setCustomerId(2L); // Different customer
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> vehicleService.updateVehicle(1L, requestDto));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void updateVehicle_DuplicateRegistration_ThrowsException() {
        // Arrange
        requestDto.setRegistrationNumber("XYZ789");
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.existsByRegistrationNumber("XYZ789")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> vehicleService.updateVehicle(1L, requestDto));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void updateVehicle_DuplicateVin_ThrowsException() {
        // Arrange
        requestDto.setVin("NEWVIN12345678901");
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.existsByVin("NEWVIN12345678901")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> vehicleService.updateVehicle(1L, requestDto));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void deleteVehicle_Success() {
        // Arrange
        when(vehicleRepository.existsById(1L)).thenReturn(true);
        doNothing().when(vehicleRepository).deleteById(1L);

        // Act
        vehicleService.deleteVehicle(1L);

        // Assert
        verify(vehicleRepository).deleteById(1L);
    }

    @Test
    void deleteVehicle_NotFound_ThrowsException() {
        // Arrange
        when(vehicleRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> vehicleService.deleteVehicle(1L));
        verify(vehicleRepository, never()).deleteById(any());
    }
}
