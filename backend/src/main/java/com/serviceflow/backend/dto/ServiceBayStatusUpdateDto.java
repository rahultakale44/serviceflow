package com.serviceflow.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating service bay status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceBayStatusUpdateDto {

    @NotNull(message = "Active status is required")
    private Boolean active;
}
