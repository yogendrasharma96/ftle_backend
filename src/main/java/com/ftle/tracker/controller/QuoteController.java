package com.ftle.tracker.controller;

import com.ftle.tracker.dto.QuoteLtpResponse;
import com.ftle.tracker.service.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @GetMapping("/public/quotes/ltp")
    public List<QuoteLtpResponse> getLtp(
            @RequestParam List<String> symbols
    ) {
        return quoteService.getLtp(symbols);
    }
}