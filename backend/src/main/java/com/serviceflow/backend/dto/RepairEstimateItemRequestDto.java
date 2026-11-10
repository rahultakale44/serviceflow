package com.serviceflow.backend.dto;

import com.serviceflow.backend.entity.EstimateItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record RepairEstimateItemRequestDto(
        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Item type is required")
        EstimateItemType itemType,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity,

        @NotNull(message = "Unit price is required")
        @PositiveOrZero(message = "Unit price must be non-negative")
        BigDecimal unitPrice
) {
}
