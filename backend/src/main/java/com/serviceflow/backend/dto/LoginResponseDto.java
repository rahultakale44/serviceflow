package com.serviceflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login response containing JWT token and user info.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String accessToken;
    private String tokenType = "Bearer";
    private UserResponseDto user;

    public LoginResponseDto(String accessToken, UserResponseDto user) {
        this.accessToken = accessToken;
        this.user = user;
    }
}
