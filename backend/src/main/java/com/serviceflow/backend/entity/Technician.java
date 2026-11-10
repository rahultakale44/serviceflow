package com.serviceflow.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Technician entity representing workshop technicians.
 * Tracks technician information, specializations, and availability status.
 * 
 * NOTE: This entity does NOT store authentication credentials.
 * Technician management is separate from application user accounts.
 * Authentication and RBAC will be implemented in Phase 9.
 */
@Entity
@Table(name = "technicians",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "technicianCode", name = "uk_technician_code"),
                @UniqueConstraint(columnNames = "email", name = "uk_technician_email")
        },
        indexes = {
                @Index(columnList = "technicianCode", name = "idx_technician_code"),
                @Index(columnList = "email", name = "idx_technician_email"),
                @Index(columnList = "specialization", name = "idx_technician_specialization"),
                @Index(columnList = "status", name = "idx_technician_status")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String technicianCode;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TechnicianSpecialization specialization;

    @Column(nullable = false)
    @Builder.Default
    private Integer experienceYears = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TechnicianStatus status = TechnicianStatus.AVAILABLE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
