package com.ftle.tracker.repository;

import com.ftle.tracker.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    @Query("select distinct t.symbol from Trade t")
    Set<String> getAllSymbol();

    long countByStatus(String status);

    // Sum of (exitPrice - entryPrice) * quantity for all Closed trades
    @Query("SELECT SUM((t.exitPrice - t.entryPrice) * t.quantity) FROM Trade t WHERE t.status = 'Closed'")
    Double calculateTotalRealizedPnL();

    // Count winning trades (where exit > entry)
    @Query("SELECT COUNT(t) FROM Trade t WHERE t.status = 'Closed' AND t.exitPrice > t.entryPrice")
    long countWinningTrades();

    // Sum of (entryPrice * quantity) for all Open trades
    @Query("SELECT SUM(t.entryPrice * t.quantity) FROM Trade t WHERE t.status = 'Open'")
    Double calculateOpenPositionsEntryValue();

    @Query("""
               SELECT t.symbol,
                      SUM((t.exitPrice - t.entryPrice) * t.quantity)
               FROM Trade t
               WHERE t.status = 'Closed'
               GROUP BY t.symbol
            """)
    List<Object[]> calculateStockWisePL();

    @Query("""
               SELECT SUM(t.entryPrice * t.quantity)
               FROM Trade t
               WHERE t.status = 'Closed'
            """)
    Double calculateTotalClosedInvestment();
}
