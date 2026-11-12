package com.serviceflow.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Security configuration for the application.
 * 
 * DEVELOPMENT CONFIGURATION:
 * This configuration permits unauthenticated access to customer, vehicle, service catalogue,
 * booking, job management, estimate, technician, service bay, and assignment endpoints
 * for development and testing purposes.
 * This is NOT production-ready.
 * 
 * FUTURE MILESTONE (Phase 9):
 * JWT-based authentication and role-based authorization will be implemented
 * to properly secure all endpoints with appropriate access controls.
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
                .requestMatchers("/api/v1/vehicles/**").permitAll() // Permit vehicle endpoints for development
                .requestMatchers("/api/v1/services/**").permitAll() // Permit service catalogue endpoints for development
                .requestMatchers("/api/v1/bookings/**").permitAll() // Permit booking endpoints for development
                .requestMatchers("/api/v1/jobs/**").permitAll() // Permit job management and estimate endpoints for development
                .requestMatchers("/api/v1/technicians/**").permitAll() // Permit technician endpoints for development
                .requestMatchers("/api/v1/service-bays/**").permitAll() // Permit service bay endpoints for development
                .requestMatchers("/api/v1/job-assignments/**").permitAll() // Permit job assignment endpoints for development
                .anyRequest().authenticated() // All other endpoints require authentication
            );

        return http.build();
    }

    /**
     * CORS configuration for local development.
     * Allows frontend running on localhost:5173 to access the API.
     * 
     * DEVELOPMENT ONLY - This will be refined in Phase 9 with proper authentication.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}