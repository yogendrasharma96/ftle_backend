package com.ftle.tracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeEx {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    private String type;

    private Double entryPrice;

    private Double exitPrice;

    private Integer quantity;

    private String status;

    private String financialYear;

    private LocalDate entryTradeDate;
    private LocalDate exitTradeDate;

    @Column(length = 1000)
    private String notes;

    private LocalDateTime createdAt;
}
