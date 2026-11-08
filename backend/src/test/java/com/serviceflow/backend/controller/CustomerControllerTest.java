package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.CustomerRequestDto;
import com.serviceflow.backend.dto.CustomerResponseDto;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.service.CustomerService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerController.
 * Tests the REST API endpoints with mocked service layer.
 */
@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private CustomerRequestDto requestDto;
    private CustomerResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = CustomerRequestDto.builder()
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .email("john.doe@example.com")
                .build();

        responseDto = CustomerResponseDto.builder()
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
    void createCustomer_ValidRequest_ReturnsCreated() {
        // Arrange
        when(customerService.createCustomer(any(CustomerRequestDto.class))).thenReturn(responseDto);

        // Act
        ResponseEntity<CustomerResponseDto> response = customerController.createCustomer(requestDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        assertEquals("John", response.getBody().getFirstName());
        verify(customerService).createCustomer(any(CustomerRequestDto.class));
    }

    @Test
    void createCustomer_DuplicatePhone_ThrowsException() {
        // Arrange
        when(customerService.createCustomer(any(CustomerRequestDto.class)))
                .thenThrow(new DuplicateResourceException("Phone", "1234567890"));

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> customerController.createCustomer(requestDto));
    }

    @Test
    void getCustomerById_ExistingCustomer_ReturnsOk() {
        // Arrange
        when(customerService.getCustomerById(1L)).thenReturn(responseDto);

        // Act
        ResponseEntity<CustomerResponseDto> response = customerController.getCustomerById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(customerService).getCustomerById(1L);
    }

    @Test
    void getCustomerById_NonExistingCustomer_ThrowsException() {
        // Arrange
        when(customerService.getCustomerById(999L))
                .thenThrow(new ResourceNotFoundException("Customer", 999L));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> customerController.getCustomerById(999L));
    }

    @Test
    void getAllCustomers_ReturnsOk() {
        // Arrange
        Page<CustomerResponseDto> page = new PageImpl<>(List.of(responseDto));
        when(customerService.getAllCustomers(any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<CustomerResponseDto>> response = customerController.getAllCustomers(0, 20, "id", "ASC");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("John", response.getBody().getContent().get(0).getFirstName());
        verify(customerService).getAllCustomers(any(Pageable.class));
    }

    @Test
    void updateCustomer_ValidRequest_ReturnsOk() {
        // Arrange
        CustomerRequestDto updateDto = CustomerRequestDto.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phone("1234567890")
                .email("jane.smith@example.com")
                .build();

        CustomerResponseDto updatedResponse = CustomerResponseDto.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .phone("1234567890")
                .email("jane.smith@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(customerService.updateCustomer(eq(1L), any(CustomerRequestDto.class)))
                .thenReturn(updatedResponse);

        // Act
        ResponseEntity<CustomerResponseDto> response = customerController.updateCustomer(1L, updateDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Jane", response.getBody().getFirstName());
        assertEquals("Smith", response.getBody().getLastName());
        verify(customerService).updateCustomer(eq(1L), any(CustomerRequestDto.class));
    }

    @Test
    void updateCustomer_NonExistingCustomer_ThrowsException() {
        // Arrange
        when(customerService.updateCustomer(eq(999L), any(CustomerRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Customer", 999L));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> customerController.updateCustomer(999L, requestDto));
    }

    @Test
    void deleteCustomer_ExistingCustomer_ReturnsNoContent() {
        // Arrange
        doNothing().when(customerService).deleteCustomer(1L);

        // Act
        ResponseEntity<Void> response = customerController.deleteCustomer(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(customerService).deleteCustomer(1L);
    }

    @Test
    void deleteCustomer_NonExistingCustomer_ThrowsException() {
        // Arrange
        doThrow(new ResourceNotFoundException("Customer", 999L))
                .when(customerService).deleteCustomer(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> customerController.deleteCustomer(999L));
    }
}
