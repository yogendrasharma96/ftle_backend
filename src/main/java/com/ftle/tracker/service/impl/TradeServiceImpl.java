package com.ftle.tracker.service.impl;

import com.ftle.tracker.dto.OpenPositionDto;
import com.ftle.tracker.dto.TradeRequest;
import com.ftle.tracker.dto.TradeStatsDTO;
import com.ftle.tracker.entity.Trade;
import com.ftle.tracker.repository.TradeRepository;
import com.ftle.tracker.service.MarketFeedService;
import com.ftle.tracker.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeServiceImpl implements TradeService {
    private final TradeRepository tradeRepository;
    private final MarketFeedService marketFeedService;

    @Override
    public Trade saveTrade(TradeRequest request) {
        log.info("Saving trade for symbol: {} in FY: {}", request.getSymbol(), request.getFinancialYear());

        Trade trade = Trade.builder()
                .symbol(request.getSymbol())
                .exchange(request.getExchange())
                .segment(request.getSegment())
                .type(request.getType())
                .entryPrice(request.getEntryPrice())
                .exitPrice(request.getExitPrice())
                .stopLoss(request.getStopLoss())
                .targetPrice(request.getTargetPrice())
                .quantity(request.getQuantity())
                .status(request.getStatus())
                .financialYear(request.getFinancialYear())
                .entryTradeDate(request.getEntryTradeDate())
                .exitTradeDate(request.getExitTradeDate())
                .notes(request.getNotes())
                .build();

        Trade savedTrade = tradeRepository.save(trade);

        // Refreshing cache ensures the 'liveQuotes' loop picks up the new symbol immediately
        marketFeedService.refreshCache();

        return savedTrade;
    }

    @Override
    public Page<Trade> getTrades(int page, int size, String financialYear) {
        Sort sort = Sort.by("entryTradeDate").descending();
        if (!financialYear.equalsIgnoreCase("ALL")) {
            return tradeRepository.findByFinancialYear(PageRequest.of(page, size, sort), financialYear);
        }
        return tradeRepository.findAll(PageRequest.of(page, size, sort));
    }

    @Override
    public Trade updateTrade(Long id, TradeRequest body) {
        Trade trade = tradeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trade with ID " + id + " not found"
                ));

        // Map the new fields from the DTO to the existing Entity
        mapToEntity(trade, body);

        Trade updatedTrade = tradeRepository.save(trade);

        // Refresh market cache in case the symbol was changed during update
        marketFeedService.refreshCache();

        return updatedTrade;
    }

    private void mapToEntity(Trade trade, TradeRequest req) {
        trade.setSymbol(req.getSymbol());
        trade.setExchange(req.getExchange());
        trade.setSegment(req.getSegment());
        trade.setType(req.getType());

        trade.setEntryPrice(req.getEntryPrice());
        trade.setExitPrice(req.getExitPrice());
        trade.setStopLoss(req.getStopLoss());
        trade.setTargetPrice(req.getTargetPrice());

        trade.setQuantity(req.getQuantity());
        trade.setStatus(req.getStatus());
        trade.setFinancialYear(req.getFinancialYear());

        trade.setEntryTradeDate(req.getEntryTradeDate());
        trade.setExitTradeDate(req.getExitTradeDate());

        trade.setNotes(req.getNotes());

    }

    @Override
    public TradeStatsDTO getGlobalStats(String financialYear) {

        TradeStatsDTO globalStats=tradeRepository.getGlobalStats(financialYear);
        List<OpenPositionDto> openPositionsSummary = tradeRepository.getOpenPositionsSummary(financialYear);
        globalStats.setOpenPositionDtos(openPositionsSummary);

        return globalStats;
    }

    @Override
    public byte[] exportTradesToExcel(String financialYear) throws Exception {

        List<Trade> trades;
        if (!financialYear.equalsIgnoreCase("ALL")) {
            trades= tradeRepository.findByFinancialYear(financialYear);
        }else {
            trades= tradeRepository.findAll();
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Trades");

        String[] columns = {
                "Symbol", "Exchange", "Segment", "Type",
                "Entry Price", "Exit Price", "Stop Loss", "Target Price",
                "Quantity", "Financial Year", "Status",
                "Entry Trade Date", "Exit Trade Date",
                "Created At", "Updated At", "Notes"
        };

        Row header = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }

        int rowIdx = 1;

        for (Trade t : trades) {

            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(t.getSymbol());
            row.createCell(1).setCellValue(t.getExchange());
            row.createCell(2).setCellValue(t.getSegment());
            row.createCell(3).setCellValue(t.getType());

            setNumeric(row.createCell(4), doubleNullChecks(t.getEntryPrice()));
            setNumeric(row.createCell(5), doubleNullChecks(t.getExitPrice()));
            setNumeric(row.createCell(6), doubleNullChecks(t.getStopLoss()));
            setNumeric(row.createCell(7), doubleNullChecks(t.getTargetPrice()));

            if (t.getQuantity() != null) {
                row.createCell(8).setCellValue(t.getQuantity());
            }

            row.createCell(9).setCellValue(t.getFinancialYear());
            row.createCell(10).setCellValue(t.getStatus());

            if (t.getEntryTradeDate() != null)
                row.createCell(11).setCellValue(t.getEntryTradeDate().toString());

            if (t.getExitTradeDate() != null)
                row.createCell(12).setCellValue(t.getExitTradeDate().toString());

            if (t.getCreatedAt() != null)
                row.createCell(13).setCellValue(t.getCreatedAt().toString());

            if (t.getUpdatedAt() != null)
                row.createCell(14).setCellValue(t.getUpdatedAt().toString());

            row.createCell(15).setCellValue(t.getNotes());
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    private Double doubleNullChecks(BigDecimal val) {
        if(val!=null){
            return val.doubleValue();
        }
        return null;
    }

    private void setNumeric(Cell cell, Double value) {
        if (value != null) cell.setCellValue(value);
    }
}
