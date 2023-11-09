package com.jr.coin.trade.bot.config.batch;

import com.jr.coin.trade.bot.domain.response.UpbitCandleMinuteResponseDto;
import com.jr.coin.trade.bot.job.quotation.CandleJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@EnableBatchProcessing
public class CandleBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CandleJob candleJob;

    public CandleBatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager, CandleJob candleJob) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.candleJob = candleJob;
    }

    @Bean
    public Job getCandleMinuteBatchJob() {
        return new JobBuilder("getCandleMinuteBatchJob", jobRepository)
                .start(getCandleMinuteBatchStep())
                .build();
    }

    @Bean
    public Step getCandleMinuteBatchStep() {
        return new StepBuilder("getCandleMinuteBatchStep", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(getCandleMinuteBatchTasklet(null), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet getCandleMinuteBatchTasklet(@Value("#{jobParameters[market]}") String market) {
        return (stepContribution, chunkContext) -> {
            int loopCount = 0;
            while (true) {
                String lastCandleTime = candleJob.getLastCandleTimeByMarket(market, loopCount);
                log.info("Last candle time: " + lastCandleTime);
                List<UpbitCandleMinuteResponseDto> responseDto = candleJob.getCandleMinute(market, lastCandleTime,200);
                int saveCount = candleJob.saveCandleMinute(responseDto);
                log.info("Market: " + market + " | " + saveCount + " candles are saved.");

                if (saveCount == 0) {
                    break;
                }

                loopCount += 1;
            }
            return RepeatStatus.FINISHED;
        };
    }

}
