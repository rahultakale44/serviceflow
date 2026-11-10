package com.serviceflow.backend.dto;

import com.serviceflow.backend.entity.JobStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ServiceJobResponseDto(
        Long id,
        String jobCardNumber,
        Long bookingId,
        Long customerId,
        String customerName,
        String customerPhone,
        Long vehicleId,
        String vehicleRegistrationNumber,
        String vehicleMake,
        String vehicleModel,
        JobStatus jobStatus,
        String complaintDescription,
        String inspectionNotes,
        String technicianNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime deliveredAt,
        List<JobInspectionResponseDto> inspections
) {
}
