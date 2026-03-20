package com.ftle.tracker.dto;

import com.ftle.tracker.entity.TradeImage;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    private String sector;
    private String financialYear;
    private LocalDate entryTradeDate;
    private LocalDate exitTradeDate;
    private String notes;

    private List<TradeImageDto> images;
}
