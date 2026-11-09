package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.CustomerRequestDto;
import com.serviceflow.backend.dto.CustomerResponseDto;
import com.serviceflow.backend.entity.Customer;
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
 * Unit tests for CustomerService.
 * Uses Mockito to mock repository dependencies.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequestDto requestDto;
    private Customer customer;

    @BeforeEach
    void setUp() {
        requestDto = CustomerRequestDto.builder()
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .email("john.doe@example.com")
                .build();

        customer = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .email("john.doe@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createCustomer_Success() {
        // Arrange
        when(customerRepository.existsByPhone(anyString())).thenReturn(false);
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // Act
        CustomerResponseDto response = customerService.createCustomer(requestDto);

        // Assert
        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("1234567890", response.getPhone());
        assertEquals("john.doe@example.com", response.getEmail());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_DuplicatePhone_ThrowsException() {
        // Arrange
        when(customerRepository.existsByPhone(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, 
                () -> customerService.createCustomer(requestDto));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void createCustomer_DuplicateEmail_ThrowsException() {
        // Arrange
        when(customerRepository.existsByPhone(anyString())).thenReturn(false);
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, 
                () -> customerService.createCustomer(requestDto));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void createCustomer_WithBlankEmail_Success() {
        // Arrange
        requestDto.setEmail("   ");
        when(customerRepository.existsByPhone(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // Act
        CustomerResponseDto response = customerService.createCustomer(requestDto);

        // Assert
        assertNotNull(response);
        verify(customerRepository, never()).existsByEmail(anyString());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void getCustomerById_Success() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // Act
        CustomerResponseDto response = customerService.getCustomerById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John", response.getFirstName());
        verify(customerRepository).findById(1L);
    }

    @Test
    void getCustomerById_NotFound_ThrowsException() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> customerService.getCustomerById(1L));
    }

    @Test
    void getAllCustomers_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Customer> customerPage = new PageImpl<>(List.of(customer));
        when(customerRepository.findAll(pageable)).thenReturn(customerPage);

        // Act
        Page<CustomerResponseDto> response = customerService.getAllCustomers(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("John", response.getContent().get(0).getFirstName());
        verify(customerRepository).findAll(pageable);
    }

    @Test
    void updateCustomer_Success() {
        // Arrange
        CustomerRequestDto updateDto = CustomerRequestDto.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phone("1234567890") // Same phone
                .email("jane.smith@example.com")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // Act
        CustomerResponseDto response = customerService.updateCustomer(1L, updateDto);

        // Assert
        assertNotNull(response);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void updateCustomer_NotFound_ThrowsException() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> customerService.updateCustomer(1L, requestDto));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_DuplicatePhone_ThrowsException() {
        // Arrange
        CustomerRequestDto updateDto = CustomerRequestDto.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phone("9876543210") // Different phone
                .email("john.doe@example.com")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByPhone("9876543210")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, 
                () -> customerService.updateCustomer(1L, updateDto));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_DuplicateEmail_ThrowsException() {
        // Arrange
        CustomerRequestDto updateDto = CustomerRequestDto.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phone("1234567890")
                .email("another@example.com") // Different email
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail("another@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, 
                () -> customerService.updateCustomer(1L, updateDto));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_Success() {
        // Arrange
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(vehicleRepository.existsByCustomerId(1L)).thenReturn(false);
        doNothing().when(customerRepository).deleteById(1L);

        // Act
        customerService.deleteCustomer(1L);

        // Assert
        verify(vehicleRepository).existsByCustomerId(1L);
        verify(customerRepository).deleteById(1L);
    }

    @Test
    void deleteCustomer_NotFound_ThrowsException() {
        // Arrange
        when(customerRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> customerService.deleteCustomer(1L));
        verify(customerRepository, never()).deleteById(any());
    }

    @Test
    void deleteCustomer_WithVehicles_ThrowsException() {
        // Arrange
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(vehicleRepository.existsByCustomerId(1L)).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> customerService.deleteCustomer(1L));
        verify(vehicleRepository).existsByCustomerId(1L);
        verify(customerRepository, never()).deleteById(any());
    }
}
