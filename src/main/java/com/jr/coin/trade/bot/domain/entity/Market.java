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
@Table(name = "MARKET")
@Data
public class Market implements Serializable {

    @Id
    @Column(name = "MARKET")
    private String market;

    @Column(name = "KOREAN_NAME")
    private String koreanName;

    @Column(name = "ENGLISH_NAME")
    private String englishName;

}
