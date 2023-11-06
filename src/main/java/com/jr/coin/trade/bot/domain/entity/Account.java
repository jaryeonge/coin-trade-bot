package com.jr.coin.trade.bot.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = false)
@Entity
@DynamicUpdate
@Table(name = "ACCOUNT")
@Data
public class Account implements Serializable {

    @Id
    @Column(name = "CURRENCY")
    private String currency;

    @Column(name = "BALANCE")
    private String balance;

    @Column(name = "LOCKED")
    private String locked;

    @Column(name = "AVG_BUY_PRICE")
    private String avgBuyPrice;

    @Column(name = "AVG_BUY_PRICE_MODIFIED")
    private String avgBuyPriceModified;

    @Column(name = "UNIT_CURRENCY")
    private String unitCurrency;

}
