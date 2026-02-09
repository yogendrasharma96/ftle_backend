package com.ftle.tracker.dto;

public record TradeStatsDTO(
        Double realizedPnL,
        Double winRate,
        Long totalClosed,
        Long totalOpen,
        Double openPositionsEntryValue // Used by frontend to calculate Live Unrealized P/L
) {}
