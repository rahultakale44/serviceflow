package com.serviceflow.backend.dto;

import com.serviceflow.backend.entity.TechnicianSpecialization;
import com.serviceflow.backend.entity.TechnicianStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a technician.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianRequestDto {

    @NotBlank(message = "Technician code is required")
    @Size(max = 20, message = "Technician code must not exceed 20 characters")
    private String technicianCode;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @NotNull(message = "Specialization is required")
    private TechnicianSpecialization specialization;

    @NotNull(message = "Experience years is required")
    @Min(value = 0, message = "Experience years must be non-negative")
    private Integer experienceYears;

    private TechnicianStatus status;

    private Boolean active;
}
