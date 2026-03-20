package com.ftle.tracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Data
public class TradeStatsDTO {

    private BigDecimal realizedPnL;
    private BigDecimal roi;
    private Long totalClosed;
    private Long totalOpen;
    private BigDecimal openPositionsEntryValue;
    private List<OpenPositionDto> openPositionDtos;

    private List<EquityPointDTO> equityCurve;
    private List<MonthlyHeatmapDTO> monthlyHeatmap;

    public TradeStatsDTO(Double realizedPnL,
                         Double totalInvestment,
                         Long totalClosed,
                         Long totalOpen,
                         Double openPositionsEntryValue) {

        this.realizedPnL = realizedPnL != null ? BigDecimal.valueOf(realizedPnL) : BigDecimal.valueOf(0.0);
        this.totalClosed = totalClosed != null ? totalClosed : 0;
        this.totalOpen = totalOpen != null ? totalOpen : 0;
        this.openPositionsEntryValue = openPositionsEntryValue != null ? BigDecimal.valueOf(openPositionsEntryValue) : BigDecimal.valueOf(0.0);
        double investment = totalInvestment == null ? 0.0 : totalInvestment;
        BigDecimal finalInvestment = BigDecimal.valueOf(investment);
        BigDecimal roiValue = BigDecimal.ZERO;
        if (realizedPnL != null &&
                finalInvestment != null &&
                finalInvestment.compareTo(BigDecimal.ZERO) != 0) {

            roiValue = this.realizedPnL
                    .divide(finalInvestment, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        this.roi = roiValue;
    }

}
