package com.ftle.tracker.service;

import com.ftle.tracker.dto.TradeRequest;
import com.ftle.tracker.dto.TradeStatsDTO;
import com.ftle.tracker.entity.Trade;
import org.springframework.data.domain.Page;


public interface TradeService {
    Trade saveTrade(TradeRequest request);

    Page<Trade> getTrades(int page, int size,String financialYear,String status);

    Trade updateTrade(Long id, TradeRequest body);

    TradeStatsDTO getGlobalStats(String financialYear);

    byte[] exportTradesToExcel(String financialYear) throws Exception;
}
