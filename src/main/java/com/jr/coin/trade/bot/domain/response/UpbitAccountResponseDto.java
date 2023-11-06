package com.jr.coin.trade.bot.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpbitAccountResponseDto {

    private String currency;
    private String balance;
    private String locked;
    private String avg_buy_price;
    private boolean avg_buy_price_modified;
    private String unit_currency;

}
