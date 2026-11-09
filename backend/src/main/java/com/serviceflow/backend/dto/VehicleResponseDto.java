package com.serviceflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for returning vehicle data.
 * Includes customer information without creating circular references.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponseDto {

    private Long id;
    private String registrationNumber;
    private String make;
    private String model;
    private Integer manufacturingYear;
    private String fuelType;
    private String vin;
    private CustomerSummaryDto customer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Nested DTO for customer summary to avoid circular references.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerSummaryDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String phone;
    }
}
