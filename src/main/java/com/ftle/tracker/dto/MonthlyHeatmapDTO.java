package com.ftle.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyHeatmapDTO {

    private int year;
    private int month;
    private double pnl;

}
