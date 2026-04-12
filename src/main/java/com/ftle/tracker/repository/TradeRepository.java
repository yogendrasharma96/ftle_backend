package com.ftle.tracker.repository;

import com.ftle.tracker.dto.OpenPositionDto;
import com.ftle.tracker.dto.TradePerformanceRow;
import com.ftle.tracker.dto.TradeStatsDTO;
import com.ftle.tracker.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TradeRepository extends JpaRepository<Trade, Long>, JpaSpecificationExecutor<Trade> {

    @Query("select distinct t.symbol from Trade t")
    Set<String> getAllSymbol();

    @Query("""
            SELECT new com.ftle.tracker.dto.OpenPositionDto(
                   t.symbol, 
                   SUM(t.quantity), 
                   CAST(SUM(t.entryPrice * t.quantity) / SUM(t.quantity) AS double)
            )
            FROM Trade t
            WHERE t.status = 'Open'
            AND (:financialYear = 'ALL' OR t.financialYear = :financialYear)
            GROUP BY t.symbol
            """)
    List<OpenPositionDto> getOpenPositionsSummary(@Param("financialYear") String financialYear);

    Page<Trade> findByFinancialYear(Pageable pageable, String financialYear);

    List<Trade> findByFinancialYear(String financialYear);

    @Query("""
            SELECT new com.ftle.tracker.dto.TradeStatsDTO(
                SUM(CASE WHEN t.status = 'Closed' THEN (t.exitPrice - t.entryPrice) * t.quantity ELSE 0.0 END),
                SUM(CASE WHEN t.status = 'Closed' THEN t.entryPrice * t.quantity ELSE 0.0 END),
                SUM(CASE WHEN t.status = 'Closed' THEN 1 ELSE 0 END),
                SUM(CASE WHEN t.status = 'Open' THEN 1 ELSE 0 END),
                SUM(CASE WHEN t.status = 'Open' THEN t.entryPrice * t.quantity ELSE 0.0 END)
            )
            FROM Trade t
            WHERE (:financialYear = 'ALL' OR t.financialYear = :financialYear)
            """)
    TradeStatsDTO getGlobalStats(@Param("financialYear") String financialYear);

    @Query("""
            SELECT
                t.exitTradeDate as exitTradeDate,
                t.entryPrice as entryPrice,
                t.exitPrice as exitPrice,
                t.quantity as quantity
            FROM Trade t
            WHERE t.status = 'Closed'
            AND (:financialYear = 'ALL' OR t.financialYear = :financialYear)
            ORDER BY t.exitTradeDate
            """)
    List<TradePerformanceRow> getPerformanceData(
            @Param("financialYear") String financialYear);

    Optional<Trade> findBySymbolAndStatusIgnoreCase(String symbol, String open);

    Page<Trade> findByFinancialYearAndStatus(PageRequest of, String financialYear, String status);

    Page<Trade> findByStatus(PageRequest of, String status);
}
