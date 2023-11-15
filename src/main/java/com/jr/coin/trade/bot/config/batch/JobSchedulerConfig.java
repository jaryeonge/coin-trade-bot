package com.jr.coin.trade.bot.config.batch;

import com.jr.coin.trade.bot.util.JobUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.UUID;

@Slf4j
@Configuration
public class JobSchedulerConfig {

    private final JobLauncher jobLauncher;
    private final Job coinTradeJob;

    public JobSchedulerConfig(JobLauncher jobLauncher, Job coinTradeJob) {
        this.jobLauncher = jobLauncher;
        this.coinTradeJob = coinTradeJob;
    }

    @Scheduled(cron = "0 * * * * *")
    public void jobScheduled() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters parameters = new JobParametersBuilder()
                .addString("id", UUID.randomUUID().toString())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(coinTradeJob, parameters);
        JobUtils.logJobExecutionDetails(jobExecution);
    }
}
