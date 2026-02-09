package com.ftle.tracker.dto;

import lombok.Data;

import java.time.LocalDate;
@Data
public class TradeRequest {
    private String symbol;
    private String type;
    private Double entryPrice;
    private Double exitPrice;
    private Integer quantity;
    private String status;
    private LocalDate tradeDate;
    private String notes;
}
