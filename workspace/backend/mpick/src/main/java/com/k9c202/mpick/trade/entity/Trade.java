package com.k9c202.mpick.trade.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.util.UUID;
import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@SuperBuilder
@NoArgsConstructor
@Table(name = "trade")
@Entity
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tradeId;

    @Column
    private UUID sellerId;

    @Column
    private Long categoryId;

    @Column
    private Integer addressId;

    @Column
    private String title;

    @Column
    private Integer price;

    @Column
    private String tradeExplain;

    @Column(name = "count", insertable = false)
    private Integer wishCount;

    @Column(insertable = false)
    private Integer viewCount;

    @Column
    private Integer tradeState;

    @CreationTimestamp
    @Column(name = "created_date", insertable = false)
    private Timestamp trade_create_date;

    @UpdateTimestamp
    @Column(name = "update_date", insertable = false)
    private Timestamp trade_update_date;

    public void increaseViewCount() { this.viewCount++; }

    public void increaseWishCount() { this.wishCount++; }

    public void decreaseWishCount() { this.wishCount--; }

    public void updateTrade(Long categoryId, Integer addressId, String title, Integer price, String tradeExplain, Integer tradeState) {
        this.categoryId = categoryId;
        this.addressId = addressId;
        this.title = title;
        this.price = price;
        this.tradeExplain = tradeExplain;
        this.tradeState = tradeState;
    }
}