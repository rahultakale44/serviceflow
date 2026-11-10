package com.serviceflow.backend.dto;

import com.serviceflow.backend.entity.TechnicianStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating technician status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianStatusUpdateDto {

    @NotNull(message = "Status is required")
    private TechnicianStatus status;

    private Boolean active;
}
