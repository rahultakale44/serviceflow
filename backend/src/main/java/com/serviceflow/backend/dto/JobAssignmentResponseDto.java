package com.serviceflow.backend.dto;

import com.serviceflow.backend.entity.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for job assignment response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobAssignmentResponseDto {

    private Long id;
    private Long jobId;
    private String jobCardNumber;
    private Long technicianId;
    private String technicianCode;
    private String technicianName;
    private Long bayId;
    private String bayCode;
    private String bayName;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private AssignmentStatus assignmentStatus;
    private String notes;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
}
