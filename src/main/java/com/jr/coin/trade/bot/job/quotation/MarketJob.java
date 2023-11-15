package com.jr.coin.trade.bot.job.quotation;

import com.jr.coin.trade.bot.domain.entity.Market;
import com.jr.coin.trade.bot.domain.response.UpbitMarketResponseDto;
import com.jr.coin.trade.bot.helper.UpbitApiClient;
import com.jr.coin.trade.bot.repository.MarketRepository;
import com.jr.coin.trade.bot.util.JobUtils;
import com.jr.coin.trade.bot.util.ScheduleCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class MarketJob {

    private final UpbitApiClient upbitApiClient;
    private final MarketRepository marketRepository;

    public MarketJob(UpbitApiClient upbitApiClient, MarketRepository marketRepository) {
        this.upbitApiClient = upbitApiClient;
        this.marketRepository = marketRepository;
    }

    public Tasklet getMarketTasklet() {
        return (stepContribution, chunkContext) -> {
            if (JobUtils.checkSchedule(ScheduleCode.DAY)) {
                List<UpbitMarketResponseDto> responseDto = getMarket();
                saveMarket(responseDto);
            }
            return RepeatStatus.FINISHED;
        };
    }

    private List<UpbitMarketResponseDto> getMarket() {
        HashMap<String, String> params = new HashMap<>();
        params.put("isDetails", "false");

        ParameterizedTypeReference<List<UpbitMarketResponseDto>> responseType = new ParameterizedTypeReference<>() {};
        Mono<List<UpbitMarketResponseDto>> result = upbitApiClient.requestGetToQuotation(responseType, "/market/all", params);
        return result.block();
    }

    private void saveMarket(List<UpbitMarketResponseDto> upbitMarketResponseDtoList) {
        upbitMarketResponseDtoList
                .forEach(upbitMarketResponseDto -> {
                    Optional<Market> optionalMarket = marketRepository.findByMarket(upbitMarketResponseDto.getMarket());
                    Market market;
                    if (optionalMarket.isEmpty()) {
                        market = new Market();
                        market.setMarket(upbitMarketResponseDto.getMarket());
                    } else {
                        market = optionalMarket.get();
                    }

                    market.setKoreanName(upbitMarketResponseDto.getKorean_name());
                    market.setEnglishName(upbitMarketResponseDto.getEnglish_name());

                    marketRepository.save(market);
                });

    }

}
