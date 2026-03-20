package com.ftle.tracker.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class TradeImage {

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    private String imageUrl;

    private String caption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    @JsonBackReference
    private Trade trade;
}
