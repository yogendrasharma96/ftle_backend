package com.ftle.tracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuoteLtpResponse {

    private String exchange_token;
    private String display_symbol;
    private String exchange;
    private String ltp;
    private String change;
    private String per_change;
}
