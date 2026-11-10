package com.serviceflow.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * BookingServiceItem entity representing a line item in a service booking.
 * Preserves service catalogue snapshots at booking time to maintain historical accuracy
 * even if the service catalogue is updated later.
 */
@Entity
@Table(name = "booking_service_items",
        indexes = {
                @Index(columnList = "bookingId", name = "idx_booking_item_booking"),
                @Index(columnList = "serviceItemId", name = "idx_booking_item_service")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookingId", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_item_booking"))
    private ServiceBooking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceItemId", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_item_service"))
    private ServiceItem serviceItem;

    // Historical snapshots - preserved from catalogue at booking time
    @Column(nullable = false, length = 20)
    private String serviceCodeSnapshot;

    @Column(nullable = false, length = 100)
    private String serviceNameSnapshot;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPriceSnapshot;

    @Column(nullable = false)
    private Integer estimatedDurationMinutesSnapshot;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    /**
     * Calculate line total based on unit price and quantity.
     */
    public void calculateLineTotal() {
        if (unitPriceSnapshot != null && quantity != null) {
            this.lineTotal = unitPriceSnapshot.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
