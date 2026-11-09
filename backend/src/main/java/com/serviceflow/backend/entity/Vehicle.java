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
 * Vehicle entity representing a vehicle owned by a customer.
 * Maintains vehicle information and its relationship with the owner (Customer).
 */
@Entity
@Table(name = "vehicles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "registrationNumber", name = "uk_vehicle_registration"),
                @UniqueConstraint(columnNames = "vin", name = "uk_vehicle_vin")
        },
        indexes = {
                @Index(columnList = "registrationNumber", name = "idx_vehicle_registration"),
                @Index(columnList = "customerId", name = "idx_vehicle_customer"),
                @Index(columnList = "vin", name = "idx_vehicle_vin")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String registrationNumber;

    @Column(nullable = false, length = 50)
    private String make;

    @Column(nullable = false, length = 50)
    private String model;

    @Column
    private Integer manufacturingYear;

    @Column(length = 20)
    private String fuelType;

    @Column(unique = true, length = 17)
    private String vin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId", nullable = false, foreignKey = @ForeignKey(name = "fk_vehicle_customer"))
    private Customer customer;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
