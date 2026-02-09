package com.ftle.tracker.controller;

import com.ftle.tracker.annotations.AdminOnly;
import com.ftle.tracker.dto.TradeRequest;
import com.ftle.tracker.dto.TradeStatsDTO;
import com.ftle.tracker.entity.Trade;
import com.ftle.tracker.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @AdminOnly
    @PostMapping("/private/trades")
    public Trade addTrade(@RequestBody TradeRequest body) {
        return tradeService.saveTrade(body);
    }

    @GetMapping("/public/trades")
    public Page<Trade> getTrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return tradeService.getTrades(page, size);
    }

    @AdminOnly
    @PutMapping("/private/trades/{id}")
    public Trade updateTrade(
            @PathVariable Long id,
            @RequestBody TradeRequest body
    ) {
        return tradeService.updateTrade(id, body);
    }

    @GetMapping("/public/stats")
    public ResponseEntity<TradeStatsDTO> getTradeStats() {
        return ResponseEntity.ok(tradeService.getGlobalStats());
    }

    @GetMapping("/private/export")
    public ResponseEntity<byte[]> exportExcel() throws Exception {

        byte[] file = tradeService.exportTradesToExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=trades.xlsx")
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        ))
                .body(file);
    }
}