package com.ftle.tracker.controller;

import com.ftle.tracker.dto.QuoteLtpResponse;
import com.ftle.tracker.service.MarketFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MarketFeedController {

    private final MarketFeedService marketFeedService;

    @GetMapping("/public/market/live")
    public List<QuoteLtpResponse> getLiveMarketData() {
        return marketFeedService.getLiveMarketData();
    }
}
