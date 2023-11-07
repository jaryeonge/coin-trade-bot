package com.jr.coin.trade.bot.config.batch;

import com.jr.coin.trade.bot.domain.response.UpbitMarketResponseDto;
import com.jr.coin.trade.bot.job.quotation.MarketJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@EnableBatchProcessing
public class MarketBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MarketJob marketJob;

    public MarketBatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager, MarketJob marketJob) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.marketJob = marketJob;
    }

    @Bean
    public Job getMarketBatchJob() {
        return new JobBuilder("getMarketBatchJob", jobRepository)
                .start(getMarketBatchStep())
                .build();
    }

    @Bean
    public Step getMarketBatchStep() {
        return new StepBuilder("getMarketBatchStep", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(getMarketBatchTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet getMarketBatchTasklet() {
        return (stepContribution, chunkContext) -> {
            List<UpbitMarketResponseDto> responseDto = marketJob.getMarket();
            marketJob.saveMarket(responseDto);
            return RepeatStatus.FINISHED;
        };
    }

}
