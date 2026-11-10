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
 * ServiceBay entity representing workshop service bays.
 * Different bays can be equipped for different types of service work.
 */
@Entity
@Table(name = "service_bays",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "bayCode", name = "uk_bay_code")
        },
        indexes = {
                @Index(columnList = "bayCode", name = "idx_bay_code"),
                @Index(columnList = "bayType", name = "idx_bay_type"),
                @Index(columnList = "active", name = "idx_bay_active")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceBay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String bayCode;

    @Column(nullable = false, length = 100)
    private String bayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BayType bayType;

    @Column(length = 255)
    private String locationDescription;

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
