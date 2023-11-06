package com.jr.coin.trade.bot.job.exchange;

import com.jr.coin.trade.bot.domain.response.UpbitAccountResponseDto;
import com.jr.coin.trade.bot.helper.UpbitApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class AccountJob {

    private final UpbitApiClient upbitApiClient;

    public AccountJob(UpbitApiClient upbitApiClient) {
        this.upbitApiClient = upbitApiClient;
    }

    public List<UpbitAccountResponseDto> getAccount() {
        HashMap<String, String> params = new HashMap<>();
        ParameterizedTypeReference<List<UpbitAccountResponseDto>> responseType = new ParameterizedTypeReference<>() {};

        return upbitApiClient.requestGetListToExchange(responseType, "/accounts", params);
    }

}
