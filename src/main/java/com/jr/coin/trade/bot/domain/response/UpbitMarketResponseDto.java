package com.jr.coin.trade.bot.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpbitMarketResponseDto {

    private String market;
    private String korean_name;
    private String english_name;

}
