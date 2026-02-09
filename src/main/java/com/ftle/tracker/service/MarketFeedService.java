package com.ftle.tracker.service;

import com.ftle.tracker.dto.QuoteLtpResponse;
import jakarta.annotation.PostConstruct;

import java.util.List;

public interface MarketFeedService {

    List<QuoteLtpResponse> getLiveMarketData();

    void refreshCache();

//    List<QuoteLtpResponse>fetchFromKotak();
}
