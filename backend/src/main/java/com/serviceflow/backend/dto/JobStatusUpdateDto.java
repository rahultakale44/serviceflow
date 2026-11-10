package com.serviceflow.backend.dto;

import com.serviceflow.backend.entity.JobStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JobStatusUpdateDto(
        @NotNull(message = "Job status is required")
        JobStatus jobStatus,

        @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        String notes
) {
}
