package com.jr.coin.trade.bot.job.quotation;

import com.jr.coin.trade.bot.domain.entity.CandleMinute;
import com.jr.coin.trade.bot.domain.response.UpbitCandleMinuteResponseDto;
import com.jr.coin.trade.bot.helper.UpbitApiClient;
import com.jr.coin.trade.bot.repository.CandleMinuteRepository;
import com.jr.coin.trade.bot.util.Constants;
import com.jr.coin.trade.bot.util.JobUtils;
import com.jr.coin.trade.bot.util.ScheduleCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class CandleJob {

    private final UpbitApiClient upbitApiClient;
    private final CandleMinuteRepository candleMinuteRepository;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter requestTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CandleJob(UpbitApiClient upbitApiClient, CandleMinuteRepository candleMinuteRepository) {
        this.upbitApiClient = upbitApiClient;
        this.candleMinuteRepository = candleMinuteRepository;
    }

    public Tasklet getBtcCandleMinuteTasklet() {
        return (stepContribution, chunkContext) -> {
            if (JobUtils.checkSchedule(ScheduleCode.MINUTE)) {
                String market = "KRW-BTC";
                Long unit = 1L;
                int loopCount = 0;
                while (true) {
                    String lastCandleTime = getLastCandleTimeByMarket(loopCount, unit);
                    log.info("Last candle time: " + lastCandleTime);
                    List<UpbitCandleMinuteResponseDto> responseDto = getCandleMinute(market, lastCandleTime,200, unit);
                    int saveCount = saveCandleMinute(responseDto);
                    log.info("Market: " + market + " | " + saveCount + " candles are saved.");

                    if (saveCount == 0) {
                        break;
                    }

                    loopCount += 1;
                }
            }
            return RepeatStatus.FINISHED;
        };
    }

    public Tasklet getBtcCandleHourTasklet() {
        return (stepContribution, chunkContext) -> {
            if (JobUtils.checkSchedule(ScheduleCode.HOUR)) {
                String market = "KRW-BTC";
                Long unit = 60L;
                int loopCount = 0;
                while (true) {
                    String lastCandleTime = getLastCandleTimeByMarket(loopCount, unit);
                    log.info("Last candle time: " + lastCandleTime);
                    List<UpbitCandleMinuteResponseDto> responseDto = getCandleMinute(market, lastCandleTime,200, unit);
                    int saveCount = saveCandleMinute(responseDto);
                    log.info("Market: " + market + " | " + saveCount + " candles are saved.");

                    if (saveCount == 0) {
                        break;
                    }

                    loopCount += 1;
                }
            }
            return RepeatStatus.FINISHED;
        };
    }

    private String getLastCandleTimeByMarket(int count, Long unit) {
        LocalDateTime localDateTime;
        if (unit == 60L) {
            localDateTime = LocalDateTime.now().withMinute(0).withSecond(0).minusHours(9);
        } else {
            localDateTime = LocalDateTime.now().withSecond(0).minusHours(9);
        }
        localDateTime = localDateTime.minusMinutes(count * unit * 200L);
        return localDateTime.format(requestTimeFormatter);
    }

    private List<UpbitCandleMinuteResponseDto> getCandleMinute(String market, String lastCandleTime, int count, Long unit) {
        String countString = count > 0 && count <= 200 ? String.valueOf(count) : "200";

        HashMap<String, String> params = new HashMap<>();
        params.put("market", market);
        params.put("to", lastCandleTime);
        params.put("count", countString);

        ParameterizedTypeReference<List<UpbitCandleMinuteResponseDto>> responseType = new ParameterizedTypeReference<>() {};
        Mono<List<UpbitCandleMinuteResponseDto>> result = upbitApiClient.requestGetToQuotation(responseType, "/candles/minutes/" + unit.toString(), params);
        return result.block();
    }

    private int saveCandleMinute(List<UpbitCandleMinuteResponseDto> upbitCandleMinuteResponseDtoList) {
        AtomicInteger saveCount = new AtomicInteger();
        upbitCandleMinuteResponseDtoList
                .forEach(upbitCandleMinuteResponseDto -> {
                    String id = upbitCandleMinuteResponseDto.getMarket() + Constants.MARKET_TIME_SPLITTER
                            + upbitCandleMinuteResponseDto.getCandle_date_time_kst() + Constants.MARKET_TIME_SPLITTER
                            + upbitCandleMinuteResponseDto.getUnit();

                    Optional<CandleMinute> optionalCandleMinute = candleMinuteRepository.findById(id);
                    CandleMinute candleMinute;
                    if (optionalCandleMinute.isEmpty()) {
                        candleMinute = new CandleMinute();
                        candleMinute.setId(id);

                        candleMinute.setMarket(upbitCandleMinuteResponseDto.getMarket());
                        candleMinute.setCandleDateTimeUtc(LocalDateTime.parse(upbitCandleMinuteResponseDto.getCandle_date_time_utc(), dateTimeFormatter));
                        candleMinute.setCandleDateTimeKst(LocalDateTime.parse(upbitCandleMinuteResponseDto.getCandle_date_time_kst(), dateTimeFormatter));
                        candleMinute.setOpeningPrice(upbitCandleMinuteResponseDto.getOpening_price());
                        candleMinute.setHighPrice(upbitCandleMinuteResponseDto.getHigh_price());
                        candleMinute.setLowPrice(upbitCandleMinuteResponseDto.getLow_price());
                        candleMinute.setTradePrice(upbitCandleMinuteResponseDto.getTrade_price());
                        candleMinute.setTimestamp(upbitCandleMinuteResponseDto.getTimestamp());
                        candleMinute.setCandleAccTradePrice(upbitCandleMinuteResponseDto.getCandle_acc_trade_price());
                        candleMinute.setCandleAccTradeVolume(upbitCandleMinuteResponseDto.getCandle_acc_trade_volume());
                        candleMinute.setUnit(upbitCandleMinuteResponseDto.getUnit());

                        candleMinuteRepository.save(candleMinute);
                        saveCount.addAndGet(1);
                    } else {
                        candleMinute = optionalCandleMinute.get();

                    }
                });
        return saveCount.get();
    }

}
