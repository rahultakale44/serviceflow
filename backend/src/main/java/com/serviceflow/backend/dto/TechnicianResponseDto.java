package com.serviceflow.backend.dto;

import com.serviceflow.backend.entity.TechnicianSpecialization;
import com.serviceflow.backend.entity.TechnicianStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for technician response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianResponseDto {

    private Long id;
    private String technicianCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private TechnicianSpecialization specialization;
    private Integer experienceYears;
    private TechnicianStatus status;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
