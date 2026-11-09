package com.serviceflow.backend.repository;

import com.serviceflow.backend.entity.ServiceItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for ServiceItem entity.
 * Provides database access methods for service catalogue operations.
 */
@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    /**
     * Find a service item by its service code.
     *
     * @param serviceCode the service code to search for
     * @return Optional containing the service item if found
     */
    Optional<ServiceItem> findByServiceCode(String serviceCode);

    /**
     * Check if a service item with the given service code exists.
     *
     * @param serviceCode the service code to check
     * @return true if exists, false otherwise
     */
    boolean existsByServiceCode(String serviceCode);

    /**
     * Find all active service items with pagination.
     *
     * @param active the active status
     * @param pageable pagination information
     * @return page of active service items
     */
    Page<ServiceItem> findByActive(Boolean active, Pageable pageable);

    /**
     * Find service items by category with pagination.
     *
     * @param category the service category
     * @param pageable pagination information
     * @return page of service items in the category
     */
    Page<ServiceItem> findByCategory(String category, Pageable pageable);

    /**
     * Find active service items by category with pagination.
     *
     * @param category the service category
     * @param active the active status
     * @param pageable pagination information
     * @return page of active service items in the category
     */
    Page<ServiceItem> findByCategoryAndActive(String category, Boolean active, Pageable pageable);
}
