package com.ftle.tracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String symbol;
    private String exchange; // NSE, BSE
    private String segment;  // EQUITY, FNO, OPTIONS
    private String type;     // BUY, SELL
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private BigDecimal stopLoss;   // Risk management
    private BigDecimal targetPrice; // Performance tracking
    private Integer quantity;
    private String financialYear; // e.g., "2024-25"
    private String status; // OPEN, CLOSED
    private LocalDate entryTradeDate;
    private LocalDate exitTradeDate;
    @Column(length = 1000)
    private String notes;
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @CreatedBy
    private String createdBy;
    @Version
    private Long version;
}
