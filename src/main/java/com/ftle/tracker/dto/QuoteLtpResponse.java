package com.ftle.tracker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuoteLtpResponse {

    private String exchange_token;
    private String display_symbol;
    private String exchange;
    private String ltp;
}
