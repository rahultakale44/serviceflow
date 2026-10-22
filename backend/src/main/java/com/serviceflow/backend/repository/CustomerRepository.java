package com.serviceflow.backend.repository;

import com.serviceflow.backend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Customer entity.
 * Provides CRUD operations and custom queries for customer data access.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find a customer by phone number.
     *
     * @param phone the phone number to search for
     * @return Optional containing the customer if found
     */
    Optional<Customer> findByPhone(String phone);

    /**
     * Find a customer by email address.
     *
     * @param email the email to search for
     * @return Optional containing the customer if found
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Check if a customer exists with the given phone number.
     *
     * @param phone the phone number to check
     * @return true if a customer exists with this phone
     */
    boolean existsByPhone(String phone);

    /**
     * Check if a customer exists with the given email.
     *
     * @param email the email to check
     * @return true if a customer exists with this email
     */
    boolean existsByEmail(String email);
}
