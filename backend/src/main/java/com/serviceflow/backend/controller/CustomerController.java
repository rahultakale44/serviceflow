package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.CustomerRequestDto;
import com.serviceflow.backend.dto.CustomerResponseDto;
import com.serviceflow.backend.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for customer management operations.
 * Provides CRUD endpoints for customer resources under /api/v1/customers.
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Create a new customer.
     *
     * @param requestDto customer data
     * @return created customer with 201 status
     */
    @PostMapping
    public ResponseEntity<CustomerResponseDto> createCustomer(
            @Valid @RequestBody CustomerRequestDto requestDto) {
        log.info("POST /api/v1/customers - Creating new customer");
        CustomerResponseDto response = customerService.createCustomer(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a customer by ID.
     *
     * @param id customer ID
     * @return customer data with 200 status
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable Long id) {
        log.info("GET /api/v1/customers/{} - Fetching customer", id);
        CustomerResponseDto response = customerService.getCustomerById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all customers with pagination.
     *
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @param sortBy field to sort by (default "id")
     * @param direction sort direction (default "ASC")
     * @return paginated list of customers with 200 status
     */
    @GetMapping
    public ResponseEntity<Page<CustomerResponseDto>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        log.info("GET /api/v1/customers - Fetching customers (page: {}, size: {})", page, size);
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<CustomerResponseDto> response = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing customer.
     *
     * @param id customer ID
     * @param requestDto updated customer data
     * @return updated customer with 200 status
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDto requestDto) {
        log.info("PUT /api/v1/customers/{} - Updating customer", id);
        CustomerResponseDto response = customerService.updateCustomer(id, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a customer by ID.
     *
     * @param id customer ID
     * @return 204 No Content status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.info("DELETE /api/v1/customers/{} - Deleting customer", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
