package com.serviceflow.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EstimateRejectionRequestDto(
        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotBlank(message = "Rejection reason is required")
        @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
        String rejectionReason,

        @Size(max = 500, message = "Customer comments cannot exceed 500 characters")
        String customerComments
) {
}
