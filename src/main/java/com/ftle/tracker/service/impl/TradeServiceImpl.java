package com.ftle.tracker.service.impl;

import com.ftle.tracker.dto.TradeRequest;
import com.ftle.tracker.dto.TradeStatsDTO;
import com.ftle.tracker.entity.Trade;
import com.ftle.tracker.repository.TradeRepository;
import com.ftle.tracker.service.MarketFeedService;
import com.ftle.tracker.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {
    private final TradeRepository tradeRepository;
    private final MarketFeedService marketFeedService;

    @Override
    public Trade saveTrade(TradeRequest request) {


            Trade trade = Trade.builder()
                    .symbol(request.getSymbol())
                    .type(request.getType())
                    .entryPrice(request.getEntryPrice())
                    .exitPrice(request.getExitPrice())
                    .quantity(request.getQuantity())
                    .status(request.getStatus())
                    .tradeDate(request.getTradeDate())
                    .notes(request.getNotes())
                    .createdAt(LocalDateTime.now())
                    .build();

        Trade savedTrade = tradeRepository.save(trade);
        marketFeedService.refreshCache();
        return savedTrade;
    }

    @Override
    public Page<Trade> getTrades(int page, int size) {
        return tradeRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Trade updateTrade(Long id, TradeRequest body) {
        Trade trade = tradeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trade not found"
                ));

        mapToEntity(trade, body);
        return tradeRepository.save(trade);
    }

    private Trade mapToEntity(Trade trade, TradeRequest req) {

        trade.setSymbol(req.getSymbol());
        trade.setType(req.getType());
        trade.setEntryPrice(req.getEntryPrice());
        trade.setExitPrice(req.getExitPrice());
        trade.setQuantity(req.getQuantity());
        trade.setTradeDate(req.getTradeDate());
        trade.setStatus(req.getStatus());
        trade.setNotes(req.getNotes());

        return trade;
    }

    @Override
    public TradeStatsDTO getGlobalStats() {
        Long totalClosed = tradeRepository.countByStatus("Closed");
        Long totalOpen = tradeRepository.countByStatus("Open");

        Double realizedPnL = tradeRepository.calculateTotalRealizedPnL();
        long wins = tradeRepository.countWinningTrades();

        Double openEntryValue = tradeRepository.calculateOpenPositionsEntryValue();

        // Avoid division by zero for Win Rate
        double winRate = (totalClosed > 0) ? ((double) wins / totalClosed) * 100 : 0;

        return new TradeStatsDTO(
                realizedPnL != null ? realizedPnL : 0.0,
                Math.round(winRate * 100.0) / 100.0, // Round to 2 decimal places
                totalClosed,
                totalOpen,
                openEntryValue != null ? openEntryValue : 0.0
        );
    }

    @Override
    public byte[] exportTradesToExcel() throws Exception {

        List<Trade> trades = tradeRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Trades");

        Row header = sheet.createRow(0);

        String[] columns = {
                "Symbol", "Type", "Entry Price","Exit Price", "Quantity",
                "Status", "Trade Date","Creation Date", "Notes"
        };

        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }

        int rowIdx = 1;

        for (Trade t : trades) {

            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(t.getSymbol());
            row.createCell(1).setCellValue(t.getType());
            setNumeric(row.createCell(2),t.getEntryPrice());
            setNumeric(row.createCell(3),t.getExitPrice());
            row.createCell(4).setCellValue(t.getQuantity());
            row.createCell(5).setCellValue(t.getStatus());
            row.createCell(6).setCellValue(t.getTradeDate().toString());
            row.createCell(7).setCellValue(t.getCreatedAt().toString());
            row.createCell(8).setCellValue(t.getNotes());
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    private void setNumeric(Cell cell, Double value) {
        if (value != null) cell.setCellValue(value);
    }
}
