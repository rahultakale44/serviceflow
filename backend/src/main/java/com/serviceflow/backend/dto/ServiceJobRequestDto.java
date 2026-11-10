package com.serviceflow.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceJobRequestDto(
        @NotNull(message = "Booking ID is required")
        Long bookingId,

        @Size(max = 1000, message = "Complaint description cannot exceed 1000 characters")
        String complaintDescription,

        @Size(max = 1000, message = "Inspection notes cannot exceed 1000 characters")
        String inspectionNotes
) {
}
