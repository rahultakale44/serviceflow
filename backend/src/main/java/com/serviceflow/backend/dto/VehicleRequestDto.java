package com.serviceflow.backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating or updating a vehicle.
 * Includes validation constraints to ensure data integrity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleRequestDto {

    @NotBlank(message = "Registration number is required")
    @Size(min = 1, max = 20, message = "Registration number must be between 1 and 20 characters")
    private String registrationNumber;

    @NotBlank(message = "Make is required")
    @Size(min = 1, max = 50, message = "Make must be between 1 and 50 characters")
    private String make;

    @NotBlank(message = "Model is required")
    @Size(min = 1, max = 50, message = "Model must be between 1 and 50 characters")
    private String model;

    @Min(value = 1900, message = "Manufacturing year must be 1900 or later")
    @Max(value = 2100, message = "Manufacturing year must be 2100 or earlier")
    private Integer manufacturingYear;

    @Size(max = 20, message = "Fuel type must not exceed 20 characters")
    private String fuelType;

    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters when provided")
    private String vin;

    @NotNull(message = "Customer ID is required")
    private Long customerId;
}
