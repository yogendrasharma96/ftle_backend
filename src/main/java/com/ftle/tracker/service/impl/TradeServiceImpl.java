package com.ftle.tracker.service.impl;

import com.ftle.tracker.dto.*;
import com.ftle.tracker.entity.Trade;
import com.ftle.tracker.entity.TradeImage;
import com.ftle.tracker.filter.TradeSpecification;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeServiceImpl implements TradeService {
    private final TradeRepository tradeRepository;
    private final MarketFeedService marketFeedService;

    @Override
    @Transactional
    public Trade saveTrade(TradeRequest request) {
        log.info("Processing trade for symbol: {} in FY: {}", request.getSymbol(), request.getFinancialYear());

        // 1. Check if an OPEN position already exists for this symbol
        // Using ignoreCase for "OPEN" to avoid string mismatch issues
        Optional<Trade> existingTradeOpt = tradeRepository.findBySymbolAndStatusIgnoreCase(
                request.getSymbol(), "OPEN");

        Trade tradeToSave;

        if (existingTradeOpt.isPresent()) {
            tradeToSave = existingTradeOpt.get();
            log.info("Existing open trade found. Averaging position for ID: {}", tradeToSave.getId());

            // 2. Calculate New Weighted Average Entry Price
            // Formula: ((Old Qty * Old Price) + (New Qty * New Price)) / (Total Qty)
            BigDecimal oldTotalCost = tradeToSave.getEntryPrice()
                    .multiply(new BigDecimal(tradeToSave.getQuantity()));
            BigDecimal newTotalCost = request.getEntryPrice()
                    .multiply(new BigDecimal(request.getQuantity()));

            int totalQuantity = tradeToSave.getQuantity() + request.getQuantity();
            BigDecimal newAvgPrice = oldTotalCost.add(newTotalCost)
                    .divide(new BigDecimal(totalQuantity), 2, RoundingMode.HALF_UP);

            // 3. Update existing entity
            tradeToSave.setQuantity(totalQuantity);
            tradeToSave.setEntryPrice(newAvgPrice);

            // Optional: Append new notes to existing notes
            if (request.getNotes() != null) {
                tradeToSave.setNotes(tradeToSave.getNotes() + " | Avg added: " + request.getNotes());
            }

            // Update dates or other metadata if necessary
            tradeToSave.setEntryTradeDate(request.getEntryTradeDate());
        } else {
            // 4. Create new trade if no open position exists
            tradeToSave = Trade.builder()
                    .symbol(request.getSymbol())
                    .exchange(request.getExchange())
                    .segment(request.getSegment())
                    .type(request.getType())
                    .entryPrice(request.getEntryPrice())
                    .quantity(request.getQuantity())
                    .status("Open")
                    .sector(request.getSector())
                    .financialYear(request.getFinancialYear())
                    .entryTradeDate(request.getEntryTradeDate())
                    .notes(request.getNotes())
                    .build();
        }

        // 5. Handle Images (Common for both new and averaged trades)
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<TradeImage> newImages = request.getImages().stream()
                    .map(img -> {
                        TradeImage image = new TradeImage();
                        image.setImageUrl(img.getImageUrl());
                        image.setCaption(img.getCaption());
                        image.setTrade(tradeToSave);
                        return image;
                    }).toList();

            if (tradeToSave.getImages() == null) {
                tradeToSave.setImages(new ArrayList<>());
            }
            tradeToSave.getImages().addAll(newImages);
        }

        // 6. Persist and Refresh
        Trade savedTrade = tradeRepository.save(tradeToSave);
        marketFeedService.refreshCache();

        return savedTrade;
    }

    @Override
    public Page<Trade> getTrades(int page, int size, String financialYear,String status) {
        Sort sort = Sort.by(
                Sort.Order.desc("entryTradeDate"),
                Sort.Order.desc("updatedAt").nullsLast()
        );
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Trade> spec = TradeSpecification.filterBy(financialYear, status);

        return tradeRepository.findAll(spec, pageable);
    }

    @Transactional
    @Override
    public Trade updateTrade(Long id, TradeRequest body) {

        Trade trade = tradeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trade with ID " + id + " not found"
                ));

        Integer existingQty = trade.getQuantity();
        Integer requestedQty = body.getQuantity();

        if (body.getImages() != null) {
            updateTradeImages(trade, body.getImages());
        }

    /*
       CASE 1: Admin is closing the trade
    */
        if ("Closed".equalsIgnoreCase(body.getStatus())) {

            if (requestedQty > existingQty) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Sell quantity exceeds available position"
                );
            }

        /*
           CASE 1A: Partial Close
        */
            if (requestedQty < existingQty) {

                Trade closedTrade = new Trade();

                // copy existing trade fields
                closedTrade.setSymbol(trade.getSymbol());
                closedTrade.setExchange(trade.getExchange());
                closedTrade.setSegment(trade.getSegment());
                closedTrade.setType(trade.getType());
                closedTrade.setSector(trade.getSector());
                closedTrade.setEntryPrice(trade.getEntryPrice());
                closedTrade.setExitPrice(body.getExitPrice());

                closedTrade.setQuantity(requestedQty);
                closedTrade.setStatus("Closed");

                closedTrade.setFinancialYear(trade.getFinancialYear());

                closedTrade.setEntryTradeDate(trade.getEntryTradeDate());
                closedTrade.setExitTradeDate(
                        body.getExitTradeDate() != null
                                ? body.getExitTradeDate()
                                : LocalDate.now()
                );

                closedTrade.setNotes(body.getNotes());

                tradeRepository.save(closedTrade);

                // reduce remaining quantity in original trade
                trade.setQuantity(existingQty - requestedQty);

                tradeRepository.save(trade);

                marketFeedService.refreshCache();

                return closedTrade;
            }

        /*
           CASE 1B: Full Close
        */
            trade.setExitPrice(body.getExitPrice());
            trade.setExitTradeDate(
                    body.getExitTradeDate() != null
                            ? body.getExitTradeDate()
                            : LocalDate.now()
            );
            trade.setStatus("Closed");

        } else {

        /*
           CASE 2: Normal update
        */
            mapToEntity(trade, body);
        }

        Trade updatedTrade = tradeRepository.save(trade);

        marketFeedService.refreshCache();

        return updatedTrade;
    }

    private void updateTradeImages(Trade trade, List<TradeImageDto> imageDtos) {
        // 1. Clear existing images if you want to replace them,
        // or skip if you only want to append.
        trade.getImages().clear();

        // 2. Map DTOs to Entities and set the back-reference
        List<TradeImage> newImages = imageDtos.stream().map(dto -> {
            TradeImage img = new TradeImage();
            img.setImageUrl(dto.getImageUrl());
            img.setTrade(trade); // CRITICAL: Link back to parent
            return img;
        }).collect(Collectors.toList());

        trade.getImages().addAll(newImages);
    }

    private void mapToEntity(Trade trade, TradeRequest req) {

        trade.setSymbol(req.getSymbol());
        trade.setExchange(req.getExchange());
        trade.setSegment(req.getSegment());
        trade.setType(req.getType());

        trade.setEntryPrice(req.getEntryPrice());

        if (req.getExitPrice() != null)
            trade.setExitPrice(req.getExitPrice());

        trade.setStopLoss(req.getStopLoss());
        trade.setTargetPrice(req.getTargetPrice());
        trade.setSector(req.getSector());
        trade.setQuantity(req.getQuantity());

        trade.setStatus(req.getStatus());
        trade.setFinancialYear(req.getFinancialYear());

        trade.setEntryTradeDate(req.getEntryTradeDate());

        if (req.getExitTradeDate() != null)
            trade.setExitTradeDate(req.getExitTradeDate());

        trade.setNotes(req.getNotes());
    }

    @Override
    public TradeStatsDTO getGlobalStats(String financialYear) {

        TradeStatsDTO globalStats = tradeRepository.getGlobalStats(financialYear);
        List<OpenPositionDto> openPositionsSummary = tradeRepository.getOpenPositionsSummary(financialYear);
        globalStats.setOpenPositionDtos(openPositionsSummary);
        getEquityAndHeatMapData(globalStats, financialYear);
        return globalStats;
    }

    public void getEquityAndHeatMapData(TradeStatsDTO stats, String financialYear) {

        List<TradePerformanceRow> rows =
                tradeRepository.getPerformanceData(financialYear);

        Map<LocalDate, Double> dailyPnL = new TreeMap<>();
        Map<String, Double> monthlyPnL = new HashMap<>();

        for (TradePerformanceRow r : rows) {

            double pnl = (r.getExitPrice() - r.getEntryPrice()) * r.getQuantity();

            LocalDate date = r.getExitTradeDate();

            // daily aggregation
            dailyPnL.merge(date, pnl, Double::sum);

            // monthly aggregation
            String key = date.getYear() + "-" + date.getMonthValue();
            monthlyPnL.merge(key, pnl, Double::sum);
        }

        // build equity curve
        List<EquityPointDTO> equity = new ArrayList<>();
        double cumulative = 0;

        for (Map.Entry<LocalDate, Double> e : dailyPnL.entrySet()) {

            cumulative += e.getValue();

            equity.add(new EquityPointDTO(
                    e.getKey(),
                    cumulative
            ));
        }

        // build heatmap
        List<MonthlyHeatmapDTO> heatmap = new ArrayList<>();

        for (Map.Entry<String, Double> e : monthlyPnL.entrySet()) {

            String[] parts = e.getKey().split("-");

            heatmap.add(new MonthlyHeatmapDTO(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    e.getValue()
            ));
        }

        stats.setEquityCurve(equity);
        stats.setMonthlyHeatmap(heatmap);

    }

    @Override
    public byte[] exportTradesToExcel(String financialYear) throws Exception {

        List<Trade> trades;
        if (!financialYear.equalsIgnoreCase("ALL")) {
            trades = tradeRepository.findByFinancialYear(financialYear);
        } else {
            trades = tradeRepository.findAll();
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
            row.createCell(16).setCellValue(t.getSector());
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
        if (val != null) {
            return val.doubleValue();
        }
        return null;
    }

    private void setNumeric(Cell cell, Double value) {
        if (value != null) cell.setCellValue(value);
    }
}
