package com.serviceflow.backend.dto;

import com.serviceflow.backend.entity.BayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a service bay.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceBayRequestDto {

    @NotBlank(message = "Bay code is required")
    @Size(max = 20, message = "Bay code must not exceed 20 characters")
    private String bayCode;

    @NotBlank(message = "Bay name is required")
    @Size(max = 100, message = "Bay name must not exceed 100 characters")
    private String bayName;

    @NotNull(message = "Bay type is required")
    private BayType bayType;

    @Size(max = 255, message = "Location description must not exceed 255 characters")
    private String locationDescription;

    private Boolean active;
}
