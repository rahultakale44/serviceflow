package com.serviceflow.backend.dto;

import com.serviceflow.backend.entity.InspectionCategory;
import com.serviceflow.backend.entity.InspectionCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JobInspectionRequestDto(
        @NotNull(message = "Inspection category is required")
        InspectionCategory category,

        @NotBlank(message = "Item name is required")
        @Size(max = 100, message = "Item name cannot exceed 100 characters")
        String itemName,

        @NotNull(message = "Condition is required")
        InspectionCondition condition,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        String notes
) {
}
