package com.ftle.tracker.service.impl;

import com.ftle.tracker.dto.QuoteLtpResponse;
import com.ftle.tracker.service.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final WebClient kotakWebClient;

    @Override
    public List<QuoteLtpResponse> getLtp(List<String> symbols) {
        String symbolsString = String.join(",", symbols);

        return kotakWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("mis.kotaksecurities.com")
                        .path("/script-details/1.0/quotes/neosymbol/{symbols}/ltp")
                        .build(symbolsString))
                .retrieve()
                .bodyToFlux(QuoteLtpResponse.class)
                .collectList()
                .block();
    }
}
