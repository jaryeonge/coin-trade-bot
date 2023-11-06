package com.jr.coin.trade.bot.config.batch;

import com.jr.coin.trade.bot.domain.response.UpbitAccountResponseDto;
import com.jr.coin.trade.bot.job.exchange.AccountJob;
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
public class AccountBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AccountJob accountJob;

    public AccountBatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager, AccountJob accountJob) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.accountJob = accountJob;
    }

    @Bean
    public Job getAccountBatchJob() {
        return new JobBuilder("getAccountBatchJob", jobRepository)
                .start(getAccountBatchStep())
                .build();
    }

    @Bean
    public Step getAccountBatchStep() {
        return new StepBuilder("getAccountBatchStep", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(getAccountBatchTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet getAccountBatchTasklet() {
        return (stepContribution, chunkContext) -> {
            List<UpbitAccountResponseDto> responseDto = accountJob.getAccount();
            accountJob.saveAccount(responseDto);
            return RepeatStatus.FINISHED;
        };
    }

}
