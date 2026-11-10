package com.serviceflow.backend.service;

import com.serviceflow.backend.entity.RepairEstimate;
import com.serviceflow.backend.entity.RepairEstimateItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * EstimateCalculationService handles all estimate financial calculations.
 * 
 * Calculation Rules:
 * 1. All calculations use BigDecimal for precision
 * 2. All monetary values are rounded to 2 decimal places using HALF_UP
 * 3. Line totals = unit price × quantity
 * 4. Subtotal = sum of all line totals
 * 5. Tax is calculated only when a valid tax rate is provided (default: zero)
 * 6. Discount is validated against configured limits (default: subtotal)
 * 7. Total = subtotal + tax - discount
 * 8. Negative totals are prevented
 * 
 * Currency Representation:
 * - All amounts use BigDecimal with scale 2
 * - Rounding strategy: HALF_UP (round to nearest, 0.5 rounds up)
 * 
 * Tax Configuration:
 * - No hardcoded tax rate
 * - Tax defaults to zero when not explicitly provided
 * - Tax rate should come from configuration or authorized request
 * - Future enhancement: tax configuration service
 * 
 * Note: This implementation does not assume GST registration or specific tax treatment.
 * Tax calculation must be configured explicitly based on business jurisdiction.
 */
@Service
public class EstimateCalculationService {

    private static final int DECIMAL_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(DECIMAL_SCALE, ROUNDING_MODE);

    /**
     * Calculate and update all financial totals for an estimate.
     * 
     * @param estimate the estimate to calculate
     */
    public void calculateEstimateTotals(RepairEstimate estimate) {
        calculateEstimateTotals(estimate, null, null);
    }

    /**
     * Calculate and update all financial totals for an estimate with optional tax and discount.
     * 
     * @param estimate the estimate to calculate
     * @param taxRatePercentage optional tax rate (e.g., 10 for 10%), defaults to zero
     * @param discountAmount optional discount amount, defaults to zero
     */
    public void calculateEstimateTotals(RepairEstimate estimate, BigDecimal taxRatePercentage, BigDecimal discountAmount) {
        // Calculate all line item totals
        estimate.getItems().forEach(this::calculateLineTotal);

        // Calculate subtotal from all line items
        BigDecimal subtotal = estimate.getItems().stream()
                .map(RepairEstimateItem::getLineTotal)
                .reduce(ZERO, BigDecimal::add);
        estimate.setSubtotal(round(subtotal));

        // Calculate tax amount (default to zero if not provided)
        BigDecimal taxAmount = ZERO;
        if (taxRatePercentage != null && taxRatePercentage.compareTo(BigDecimal.ZERO) > 0) {
            taxAmount = subtotal.multiply(taxRatePercentage)
                    .divide(new BigDecimal("100"), DECIMAL_SCALE, ROUNDING_MODE);
        }
        estimate.setTaxAmount(round(taxAmount));

        // Validate and set discount amount
        BigDecimal validatedDiscount = validateDiscount(subtotal, discountAmount);
        estimate.setDiscountAmount(round(validatedDiscount));

        // Calculate total amount (subtotal + tax - discount)
        BigDecimal totalAmount = subtotal.add(taxAmount).subtract(validatedDiscount);
        
        // Prevent negative totals
        if (totalAmount.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Total amount cannot be negative. Check discount amount.");
        }
        
        estimate.setTotalAmount(round(totalAmount));
    }

    /**
     * Calculate line total for an estimate item.
     * Line total = unit price × quantity
     * 
     * @param item the estimate item to calculate
     */
    public void calculateLineTotal(RepairEstimateItem item) {
        if (item.getUnitPrice() == null) {
            throw new IllegalArgumentException("Unit price is required");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        BigDecimal lineTotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        item.setLineTotal(round(lineTotal));
    }

    /**
     * Validate discount amount.
     * Discount cannot exceed subtotal.
     * 
     * @param subtotal the subtotal amount
     * @param discountAmount the requested discount amount
     * @return validated discount amount (defaults to zero if null or invalid)
     */
    private BigDecimal validateDiscount(BigDecimal subtotal, BigDecimal discountAmount) {
        if (discountAmount == null || discountAmount.compareTo(ZERO) <= 0) {
            return ZERO;
        }

        if (discountAmount.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException(
                    "Discount amount cannot exceed subtotal. Subtotal: " + subtotal + ", Discount: " + discountAmount);
        }

        return discountAmount;
    }

    /**
     * Round a BigDecimal value to configured scale using configured rounding mode.
     * 
     * @param value the value to round
     * @return rounded value
     */
    private BigDecimal round(BigDecimal value) {
        return value.setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }
}
