package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.CustomerRequestDto;
import com.serviceflow.backend.dto.CustomerResponseDto;
import com.serviceflow.backend.entity.Customer;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.exception.ResourceNotFoundException;
import com.serviceflow.backend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for customer management operations.
 * Implements business logic, validation, and transaction management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Create a new customer.
     * Validates uniqueness of phone and email before creation.
     *
     * @param requestDto the customer data
     * @return the created customer response
     * @throws DuplicateResourceException if phone or email already exists
     */
    @Transactional
    public CustomerResponseDto createCustomer(CustomerRequestDto requestDto) {
        log.info("Creating customer with phone: {}", requestDto.getPhone());

        // Validate phone uniqueness
        if (customerRepository.existsByPhone(requestDto.getPhone())) {
            throw new DuplicateResourceException("Phone", requestDto.getPhone());
        }

        // Validate email uniqueness (only if email is provided and not blank)
        if (requestDto.getEmail() != null && !requestDto.getEmail().isBlank()) {
            if (customerRepository.existsByEmail(requestDto.getEmail())) {
                throw new DuplicateResourceException("Email", requestDto.getEmail());
            }
        }

        Customer customer = Customer.builder()
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .phone(requestDto.getPhone())
                .email(normalizeEmail(requestDto.getEmail()))
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully with id: {}", savedCustomer.getId());

        return mapToResponseDto(savedCustomer);
    }

    /**
     * Get a customer by ID.
     *
     * @param id the customer ID
     * @return the customer response
     * @throws ResourceNotFoundException if customer not found
     */
    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerById(Long id) {
        log.info("Fetching customer with id: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        return mapToResponseDto(customer);
    }

    /**
     * Get all customers with pagination.
     *
     * @param pageable pagination parameters
     * @return page of customer responses
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponseDto> getAllCustomers(Pageable pageable) {
        log.info("Fetching customers with pagination: {}", pageable);
        return customerRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }

    /**
     * Update an existing customer.
     * Validates uniqueness of phone and email if changed.
     *
     * @param id the customer ID
     * @param requestDto the updated customer data
     * @return the updated customer response
     * @throws ResourceNotFoundException if customer not found
     * @throws DuplicateResourceException if phone or email conflicts with another customer
     */
    @Transactional
    public CustomerResponseDto updateCustomer(Long id, CustomerRequestDto requestDto) {
        log.info("Updating customer with id: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        // Check phone uniqueness if phone is being changed
        if (!customer.getPhone().equals(requestDto.getPhone())) {
            if (customerRepository.existsByPhone(requestDto.getPhone())) {
                throw new DuplicateResourceException("Phone", requestDto.getPhone());
            }
            customer.setPhone(requestDto.getPhone());
        }

        // Check email uniqueness if email is being changed
        String normalizedEmail = normalizeEmail(requestDto.getEmail());
        String currentEmail = customer.getEmail();
        
        // Only check if the email is actually changing
        if ((normalizedEmail == null && currentEmail != null) ||
            (normalizedEmail != null && !normalizedEmail.equals(currentEmail))) {
            
            if (normalizedEmail != null && customerRepository.existsByEmail(normalizedEmail)) {
                throw new DuplicateResourceException("Email", normalizedEmail);
            }
            customer.setEmail(normalizedEmail);
        }

        // Update other fields
        customer.setFirstName(requestDto.getFirstName());
        customer.setLastName(requestDto.getLastName());

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated successfully with id: {}", updatedCustomer.getId());

        return mapToResponseDto(updatedCustomer);
    }

    /**
     * Delete a customer by ID.
     *
     * @param id the customer ID
     * @throws ResourceNotFoundException if customer not found
     */
    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with id: {}", id);

        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer", id);
        }

        customerRepository.deleteById(id);
        log.info("Customer deleted successfully with id: {}", id);
    }

    /**
     * Map Customer entity to CustomerResponseDto.
     */
    private CustomerResponseDto mapToResponseDto(Customer customer) {
        return CustomerResponseDto.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    /**
     * Normalize email: convert blank strings to null for consistent handling.
     */
    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim();
    }
}
