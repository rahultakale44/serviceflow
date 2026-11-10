package com.serviceflow.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating a job assignment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobAssignmentRequestDto {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    @NotNull(message = "Technician ID is required")
    private Long technicianId;

    @NotNull(message = "Bay ID is required")
    private Long bayId;

    @NotNull(message = "Scheduled start is required")
    private LocalDateTime scheduledStart;

    @NotNull(message = "Scheduled end is required")
    private LocalDateTime scheduledEnd;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
