package com.serviceflow.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record RepairEstimateRequestDto(
        @NotEmpty(message = "At least one estimate item is required")
        @Valid
        List<RepairEstimateItemRequestDto> items,

        @PositiveOrZero(message = "Tax rate percentage must be non-negative")
        BigDecimal taxRatePercentage,

        @PositiveOrZero(message = "Discount amount must be non-negative")
        BigDecimal discountAmount,

        @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        String notes
) {
}
