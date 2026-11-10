package com.serviceflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary DTO for vehicle information in booking response.
 * Avoids recursive JSON relationships.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleSummaryDto {

    private Long id;
    private String registrationNumber;
    private String make;
    private String model;
    private Integer manufacturingYear;
}
