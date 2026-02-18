package com.ftle.tracker.dto;

import java.util.Map;

public record TradeStatsDTO(
        Double realizedPnL,
        Double roi,
        Long totalClosed,
        Long totalOpen,
        Double openPositionsEntryValue,
        Map<String,Double> stockWiseProfit
) {}
