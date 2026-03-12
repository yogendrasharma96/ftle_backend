package com.ftle.tracker.dto;

import lombok.Data;

@Data
public class MarketViewDto {

    private String title;

    private String content;

    private String tags;

    private String sentiment;

    private boolean draft;

}
