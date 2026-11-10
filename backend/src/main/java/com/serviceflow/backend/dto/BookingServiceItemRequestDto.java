package com.serviceflow.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for booking service item (line item in a booking).
 * Client provides service ID and quantity only.
 * Prices and durations are read from the database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingServiceItemRequestDto {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
