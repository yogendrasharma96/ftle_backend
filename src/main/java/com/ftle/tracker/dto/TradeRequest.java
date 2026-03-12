package com.ftle.tracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TradeRequest {
    private String symbol;
    private String exchange;
    private String segment;
    private String type;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private BigDecimal stopLoss;
    private BigDecimal targetPrice;
    private Integer quantity;
    private String status;
    private String financialYear;
    private LocalDate entryTradeDate;
    private LocalDate exitTradeDate;
    private String notes;
}
