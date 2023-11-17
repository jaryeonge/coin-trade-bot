package com.jr.coin.trade.bot.config.batch;

import com.jr.coin.trade.bot.job.exchange.AccountJob;
import com.jr.coin.trade.bot.job.quotation.CandleJob;
import com.jr.coin.trade.bot.job.quotation.MarketJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final static int CORE_POOL_SIZE = 15;
    private final static int MAX_POOL_SIZE = 20;
    private final static int QUEUE_CAPACITY = 30;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AccountJob accountJob;
    private final CandleJob candleJob;
    private final MarketJob marketJob;

    public BatchConfig(
            JobRepository jobRepository, PlatformTransactionManager transactionManager, AccountJob accountJob,
            CandleJob candleJob, MarketJob marketJob
    ) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.accountJob = accountJob;
        this.candleJob = candleJob;
        this.marketJob = marketJob;
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(CORE_POOL_SIZE);
        taskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        taskExecutor.setQueueCapacity(QUEUE_CAPACITY);
        taskExecutor.setThreadNamePrefix("job-thread-");
        return taskExecutor;
    }

    @Bean
    public Job coinTradeJob() {
        return new JobBuilder("coinTradeJob", jobRepository)
                .start(dataFlow())
                .build()
                .build();
    }

    @Bean
    public Flow dataFlow() {
        return new FlowBuilder<SimpleFlow>("dataFlow")
                .split(taskExecutor())
                .add(accountFlow(), btcCandleMinuteFlow(), btcCandleHourFlow(), marketFlow())
                .build();
    }

    @Bean
    public Flow accountFlow() {
        return new FlowBuilder<SimpleFlow>("accountFlow")
                .start(getAccountStep())
                .build();
    }

    @Bean
    public Step getAccountStep() {
        return new StepBuilder("getAccountStep", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(accountJob.getAccountTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Flow btcCandleMinuteFlow() {
        return new FlowBuilder<SimpleFlow>("btcCandleMinuteFlow")
                .start(getBtcCandleMinuteStep())
                .build();
    }

    @Bean
    public Step getBtcCandleMinuteStep() {
        return new StepBuilder("getBtcCandleMinuteStep", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(candleJob.getBtcCandleMinuteTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Flow btcCandleHourFlow() {
        return new FlowBuilder<SimpleFlow>("btcCandleHourFlow")
                .start(getBtcCandleHourStep())
                .build();
    }

    @Bean
    public Step getBtcCandleHourStep() {
        return new StepBuilder("getBtcCandleHourStep", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(candleJob.getBtcCandleHourTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Flow marketFlow() {
        return new FlowBuilder<SimpleFlow>("marketFlow")
                .start(getMarketStep())
                .build();
    }

    @Bean
    public Step getMarketStep() {
        return new StepBuilder("getMarketStep", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(marketJob.getMarketTasklet(), transactionManager)
                .build();
    }

}
