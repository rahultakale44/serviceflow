package com.serviceflow.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EstimateApprovalRequestDto(
        @NotNull(message = "Customer ID is required")
        Long customerId,

        @Size(max = 500, message = "Customer comments cannot exceed 500 characters")
        String customerComments
) {
}
