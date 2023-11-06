package com.jr.coin.trade.bot.domain.response;

import com.jr.coin.trade.bot.domain.vo.UpbitErrorVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpbitErrorResponseDto {

    private UpbitErrorVO error;

}
