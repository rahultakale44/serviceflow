package com.serviceflow.backend.service;

import com.serviceflow.backend.dto.*;
import com.serviceflow.backend.entity.User;
import com.serviceflow.backend.exception.DuplicateResourceException;
import com.serviceflow.backend.repository.UserRepository;
import com.serviceflow.backend.security.JwtTokenProvider;
import com.serviceflow.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for user authentication operations.
 * Handles login, registration, and token management.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    /**
     * Authenticate user and generate JWT token.
     *
     * @param request the login request containing username and password
     * @return LoginResponseDto with JWT token and user info
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = tokenProvider.generateToken(authentication);

        // Update last login timestamp
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        UserResponseDto userDto = UserResponseDto.fromEntity(user);

        return new LoginResponseDto(jwt, userDto);
    }

    /**
     * Register a new user.
     * Only admins can register new users in a production system.
     *
     * @param request the registration request
     * @return UserResponseDto of the created user
     * @throws DuplicateResourceException if username or email already exists
     */
    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return UserResponseDto.fromEntity(savedUser);
    }

    /**
     * Get current authenticated user.
     *
     * @return UserResponseDto of the current user
     */
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserResponseDto.fromEntity(user);
    }
}
