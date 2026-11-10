package com.serviceflow.backend.service;

import com.serviceflow.backend.entity.EstimateItemType;
import com.serviceflow.backend.entity.RepairEstimate;
import com.serviceflow.backend.entity.RepairEstimateItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class EstimateCalculationServiceTest {

    private EstimateCalculationService calculationService;

    @BeforeEach
    void setUp() {
        calculationService = new EstimateCalculationService();
    }

    @Test
    void calculateLineTotal_Success() {
        RepairEstimateItem item = RepairEstimateItem.builder()
                .description("Oil Change")
                .itemType(EstimateItemType.LABOUR)
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .build();

        calculationService.calculateLineTotal(item);

        assertEquals(new BigDecimal("100.00"), item.getLineTotal());
    }

    @Test
    void calculateLineTotal_InvalidQuantity() {
        RepairEstimateItem item = RepairEstimateItem.builder()
                .description("Oil Change")
                .itemType(EstimateItemType.LABOUR)
                .quantity(0)
                .unitPrice(new BigDecimal("50.00"))
                .build();

        assertThrows(IllegalArgumentException.class, () -> calculationService.calculateLineTotal(item));
    }

    @Test
    void calculateLineTotal_NullPrice() {
        RepairEstimateItem item = RepairEstimateItem.builder()
                .description("Oil Change")
                .itemType(EstimateItemType.LABOUR)
                .quantity(2)
                .unitPrice(null)
                .build();

        assertThrows(IllegalArgumentException.class, () -> calculationService.calculateLineTotal(item));
    }

    @Test
    void calculateEstimateTotals_WithoutTaxOrDiscount() {
        RepairEstimate estimate = RepairEstimate.builder().build();
        
        RepairEstimateItem item1 = RepairEstimateItem.builder()
                .description("Labour")
                .itemType(EstimateItemType.LABOUR)
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .build();
        
        RepairEstimateItem item2 = RepairEstimateItem.builder()
                .description("Parts")
                .itemType(EstimateItemType.PART)
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        estimate.addItem(item1);
        estimate.addItem(item2);

        calculationService.calculateEstimateTotals(estimate);

        assertEquals(new BigDecimal("200.00"), estimate.getSubtotal());
        assertEquals(new BigDecimal("0.00"), estimate.getTaxAmount());
        assertEquals(new BigDecimal("0.00"), estimate.getDiscountAmount());
        assertEquals(new BigDecimal("200.00"), estimate.getTotalAmount());
    }

    @Test
    void calculateEstimateTotals_WithTax() {
        RepairEstimate estimate = RepairEstimate.builder().build();
        
        RepairEstimateItem item = RepairEstimateItem.builder()
                .description("Labour")
                .itemType(EstimateItemType.LABOUR)
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        estimate.addItem(item);

        calculationService.calculateEstimateTotals(estimate, new BigDecimal("10"), null);

        assertEquals(new BigDecimal("100.00"), estimate.getSubtotal());
        assertEquals(new BigDecimal("10.00"), estimate.getTaxAmount());
        assertEquals(new BigDecimal("0.00"), estimate.getDiscountAmount());
        assertEquals(new BigDecimal("110.00"), estimate.getTotalAmount());
    }

    @Test
    void calculateEstimateTotals_WithDiscount() {
        RepairEstimate estimate = RepairEstimate.builder().build();
        
        RepairEstimateItem item = RepairEstimateItem.builder()
                .description("Labour")
                .itemType(EstimateItemType.LABOUR)
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        estimate.addItem(item);

        calculationService.calculateEstimateTotals(estimate, null, new BigDecimal("10.00"));

        assertEquals(new BigDecimal("100.00"), estimate.getSubtotal());
        assertEquals(new BigDecimal("0.00"), estimate.getTaxAmount());
        assertEquals(new BigDecimal("10.00"), estimate.getDiscountAmount());
        assertEquals(new BigDecimal("90.00"), estimate.getTotalAmount());
    }

    @Test
    void calculateEstimateTotals_WithTaxAndDiscount() {
        RepairEstimate estimate = RepairEstimate.builder().build();
        
        RepairEstimateItem item = RepairEstimateItem.builder()
                .description("Labour")
                .itemType(EstimateItemType.LABOUR)
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        estimate.addItem(item);

        calculationService.calculateEstimateTotals(estimate, new BigDecimal("10"), new BigDecimal("20.00"));

        assertEquals(new BigDecimal("100.00"), estimate.getSubtotal());
        assertEquals(new BigDecimal("10.00"), estimate.getTaxAmount());
        assertEquals(new BigDecimal("20.00"), estimate.getDiscountAmount());
        assertEquals(new BigDecimal("90.00"), estimate.getTotalAmount());
    }

    @Test
    void calculateEstimateTotals_DiscountExceedsSubtotal() {
        RepairEstimate estimate = RepairEstimate.builder().build();
        
        RepairEstimateItem item = RepairEstimateItem.builder()
                .description("Labour")
                .itemType(EstimateItemType.LABOUR)
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        estimate.addItem(item);

        assertThrows(IllegalArgumentException.class, 
                () -> calculationService.calculateEstimateTotals(estimate, null, new BigDecimal("150.00")));
    }

    @Test
    void calculateEstimateTotals_NegativeTotalPrevented() {
        RepairEstimate estimate = RepairEstimate.builder().build();
        
        RepairEstimateItem item = RepairEstimateItem.builder()
                .description("Labour")
                .itemType(EstimateItemType.LABOUR)
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        estimate.addItem(item);

        // This should throw because discount equals subtotal, resulting in zero, but with negative tax it would be negative
        // Actually this test demonstrates that discount cannot exceed subtotal which is validated
        assertThrows(IllegalArgumentException.class,
                () -> calculationService.calculateEstimateTotals(estimate, null, new BigDecimal("150.00")));
    }

    @Test
    void calculateEstimateTotals_RoundingHalfUp() {
        RepairEstimate estimate = RepairEstimate.builder().build();
        
        RepairEstimateItem item = RepairEstimateItem.builder()
                .description("Labour")
                .itemType(EstimateItemType.LABOUR)
                .quantity(3)
                .unitPrice(new BigDecimal("33.33"))
                .build();

        estimate.addItem(item);

        calculationService.calculateEstimateTotals(estimate, new BigDecimal("10"), null);

        // 33.33 * 3 = 99.99, tax = 9.999 rounds to 10.00, total = 109.99
        assertEquals(new BigDecimal("99.99"), estimate.getSubtotal());
        assertEquals(new BigDecimal("10.00"), estimate.getTaxAmount());
        assertEquals(new BigDecimal("109.99"), estimate.getTotalAmount());
    }
}
