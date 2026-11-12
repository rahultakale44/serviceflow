package com.serviceflow.backend.controller;

import com.serviceflow.backend.dto.LoginRequestDto;
import com.serviceflow.backend.dto.LoginResponseDto;
import com.serviceflow.backend.dto.RegisterRequestDto;
import com.serviceflow.backend.dto.UserResponseDto;
import com.serviceflow.backend.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * Handles user login, registration, and current user information.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Login endpoint.
     *
     * @param request the login request containing username and password
     * @return LoginResponseDto with JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Register a new user.
     * Only admins can register new users.
     *
     * @param request the registration request
     * @return UserResponseDto of the created user
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        UserResponseDto response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get current authenticated user information.
     *
     * @return UserResponseDto of the current user
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        UserResponseDto response = authenticationService.getCurrentUser();
        return ResponseEntity.ok(response);
    }
}
