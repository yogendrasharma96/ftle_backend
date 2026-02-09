package com.ftle.tracker.service;

import com.ftle.tracker.dto.QuoteLtpResponse;

import java.util.List;

public interface QuoteService {
    List<QuoteLtpResponse> getLtp(List<String> symbols);
}
