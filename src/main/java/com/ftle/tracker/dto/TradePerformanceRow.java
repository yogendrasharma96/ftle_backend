package com.ftle.tracker.dto;

import java.time.LocalDate;

public interface TradePerformanceRow {

    LocalDate getExitTradeDate();

    Double getEntryPrice();

    Double getExitPrice();

    Integer getQuantity();
}
