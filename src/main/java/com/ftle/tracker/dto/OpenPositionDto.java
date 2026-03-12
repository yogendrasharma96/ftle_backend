package com.ftle.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  // Useful for JSON deserialization
@AllArgsConstructor // Useful for manual object creation
public class OpenPositionDto {

    private String symbol;
    private Long quantity;
    private Double avgEntryPrice;

    // Manual constructor for JPQL 'SELECT new' if types need specific mapping
    public OpenPositionDto(String symbol, Object quantity, Object avgEntryPrice) {
        this.symbol = symbol;

        // Handle Hibernate return types safely
        this.quantity = (quantity instanceof Number) ? ((Number) quantity).longValue() : 0L;

        if (avgEntryPrice instanceof Number) {
            this.avgEntryPrice = ((Number) avgEntryPrice).doubleValue();
        } else {
            this.avgEntryPrice = 0.0;
        }
    }
}
