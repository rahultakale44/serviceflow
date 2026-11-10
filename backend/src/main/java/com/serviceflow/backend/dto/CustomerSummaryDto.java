package com.serviceflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary DTO for customer information in booking response.
 * Avoids recursive JSON relationships.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSummaryDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
}
