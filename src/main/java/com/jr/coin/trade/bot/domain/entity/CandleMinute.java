package com.jr.coin.trade.bot.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false)
@Entity
@DynamicUpdate
@Table(name = "CANDLE_MINUTE", indexes = @Index(name = "CANDLE_DATE_TIME_KST_INDEX", columnList = "CANDLE_DATE_TIME_KST"))
@Data
public class CandleMinute implements Serializable {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "MARKET")
    private String market;

    @Column(name = "CANDLE_DATE_TIME_UTC")
    private LocalDateTime candleDateTimeUtc;

    @Column(name = "CANDLE_DATE_TIME_KST")
    private LocalDateTime candleDateTimeKst;

    @Column(name = "OPENING_PRICE")
    private Double openingPrice;

    @Column(name = "HIGH_PRICE")
    private Double highPrice;

    @Column(name = "LOW_PRICE")
    private Double lowPrice;

    @Column(name = "TRADE_PRICE")
    private Double tradePrice;

    @Column(name = "TIMESTAMP")
    private Long timestamp;

    @Column(name = "CANDLE_ACC_TRADE_PRICE")
    private Double candleAccTradePrice;

    @Column(name = "CANDLE_ACC_TRADE_VOLUME")
    private Double candleAccTradeVolume;

    @Column(name = "UNIT")
    private Integer unit;

}
