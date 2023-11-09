package com.jr.coin.trade.bot.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpbitCandleMinuteResponseDto {

    private String market;
    private String candle_date_time_utc;
    private String candle_date_time_kst;
    private Double opening_price;
    private Double high_price;
    private Double low_price;
    private Double trade_price;
    private Long timestamp;
    private Double candle_acc_trade_price;
    private Double candle_acc_trade_volume;
    private Integer unit;

}
