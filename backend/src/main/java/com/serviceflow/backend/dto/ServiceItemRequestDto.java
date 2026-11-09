package com.serviceflow.backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for creating or updating a service item.
 * Includes validation constraints to ensure data integrity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceItemRequestDto {

    @NotBlank(message = "Service code is required")
    @Size(min = 1, max = 20, message = "Service code must be between 1 and 20 characters")
    private String serviceCode;

    @NotBlank(message = "Service name is required")
    @Size(min = 1, max = 100, message = "Service name must be between 1 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(min = 1, max = 50, message = "Category must be between 1 and 50 characters")
    private String category;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Base price must be zero or positive")
    @Digits(integer = 8, fraction = 2, message = "Base price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal basePrice;

    @NotNull(message = "Estimated duration is required")
    @Min(value = 1, message = "Estimated duration must be at least 1 minute")
    @Max(value = 10080, message = "Estimated duration must not exceed 10080 minutes (1 week)")
    private Integer estimatedDurationMinutes;

    private Boolean active;
}
