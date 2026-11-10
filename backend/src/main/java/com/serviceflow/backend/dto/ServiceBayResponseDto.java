package com.serviceflow.backend.dto;

import com.serviceflow.backend.entity.BayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for service bay response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceBayResponseDto {

    private Long id;
    private String bayCode;
    private String bayName;
    private BayType bayType;
    private String locationDescription;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
