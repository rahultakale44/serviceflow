package com.serviceflow.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ServiceBooking entity representing a customer's booking for vehicle services.
 * Contains booking details, relationships with customer and vehicle,
 * and aggregated pricing/duration information.
 */
@Entity
@Table(name = "service_bookings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "bookingNumber", name = "uk_booking_number")
        },
        indexes = {
                @Index(columnList = "bookingNumber", name = "idx_booking_number"),
                @Index(columnList = "customerId", name = "idx_booking_customer"),
                @Index(columnList = "vehicleId", name = "idx_booking_vehicle"),
                @Index(columnList = "bookingStatus", name = "idx_booking_status"),
                @Index(columnList = "bookingDateTime", name = "idx_booking_datetime")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String bookingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_customer"))
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicleId", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_vehicle"))
    private Vehicle vehicle;

    @Column(nullable = false)
    private LocalDateTime bookingDateTime;

    @Column(columnDefinition = "TEXT")
    private String customerNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus bookingStatus = BookingStatus.REQUESTED;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedTotalPrice;

    @Column(nullable = false)
    private Integer estimatedTotalDurationMinutes;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookingServiceItem> bookingServiceItems = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Helper method to add a booking service item.
     * Maintains bidirectional relationship.
     */
    public void addBookingServiceItem(BookingServiceItem item) {
        bookingServiceItems.add(item);
        item.setBooking(this);
    }
}
