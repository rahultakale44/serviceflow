package com.serviceflow.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application.
 * 
 * DEVELOPMENT CONFIGURATION:
 * This configuration permits unauthenticated access to customer endpoints
 * for development and testing purposes. This is NOT production-ready.
 * 
 * FUTURE MILESTONE:
 * JWT-based authentication and role-based authorization will be implemented
 * in a separate milestone to properly secure all endpoints.
 * 
 * All other endpoints remain protected by Spring Security's default authentication.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for REST API
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/customers/**").permitAll() // Permit customer endpoints for development
                .anyRequest().authenticated() // All other endpoints require authentication
            );

        return http.build();
    }
}
