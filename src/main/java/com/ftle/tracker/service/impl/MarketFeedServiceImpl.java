package com.ftle.tracker.service.impl;

import com.ftle.tracker.dto.QuoteLtpResponse;
import com.ftle.tracker.dto.ScripMasterDto;
import com.ftle.tracker.repository.TradeRepository;
import com.ftle.tracker.service.MarketFeedService;
import com.ftle.tracker.service.QuoteService;
import com.ftle.tracker.service.ScripMasterService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketFeedServiceImpl implements MarketFeedService {

    private final TradeRepository tradeRepository;
    private final ScripMasterService scripMasterService;
    private final QuoteService quoteService;

    private final AtomicReference<List<QuoteLtpResponse>> latestQuotes =
            new AtomicReference<>(List.of());

    @PostConstruct
    public void init() {
        try {
            List<QuoteLtpResponse> data = fetchFromKotak();
            latestQuotes.set(data);
            log.info("Initial market data loaded: {}", data.size());
        } catch (Exception e) {
            log.error("Initial load failed", e);
        }
    }
    @Override
    public void refreshCache() {
        try {
            List<QuoteLtpResponse> fresh = fetchFromKotak();

            if (!fresh.isEmpty()) {
                latestQuotes.set(fresh);
            }

        } catch (Exception e) {
            log.error("Market refresh failed – keeping old cache", e);
        }
    }
    @Scheduled(cron = "*/5 * 9-15 ? * MON-FRI", zone = "Asia/Kolkata")
    public void refreshMarketData() {
        if (!isMarketOpen()) return;
        refreshCache();
    }

    private boolean isMarketOpen() {
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));
        return !now.isBefore(LocalTime.of(9,15))
                && !now.isAfter(LocalTime.of(15,15));
    }

    private List<QuoteLtpResponse> fetchFromKotak() {

        Set<String> tradeSymbols = tradeRepository.getAllSymbol();
        if (tradeSymbols.isEmpty()) return latestQuotes.get();

        List<ScripMasterDto> filtered =
                scripMasterService.getScripMasterData()
                        .stream()
                        .filter(x -> tradeSymbols.contains(x.getPTrdSymbol()))
                        .toList();

        if (filtered.isEmpty()) return latestQuotes.get();

        List<String> symbols =
                filtered.stream()
                        .map(x -> x.getPExchSeg() + "|" + x.getPSymbol())
                        .toList();

        return quoteService.getLtp(symbols);
    }

    @Override
    public List<QuoteLtpResponse> getLiveMarketData() {
        return latestQuotes.get();
    }
}