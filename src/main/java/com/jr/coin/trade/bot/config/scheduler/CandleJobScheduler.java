package com.jr.coin.trade.bot.config.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

@Slf4j
@Configuration
public class CandleJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job getCandleMinuteBatchJob;

    public CandleJobScheduler(JobLauncher jobLauncher, Job getCandleMinuteBatchJob) {
        this.jobLauncher = jobLauncher;
        this.getCandleMinuteBatchJob = getCandleMinuteBatchJob;
    }

    @Scheduled(cron = "0 * * * * *")
    public void jobScheduled() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters parameters = new JobParametersBuilder()
                .addString("market", "KRW-BTC")
                .addDate("date", new Date())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(getCandleMinuteBatchJob, parameters);

        while (jobExecution.isRunning()) {
            log.info("...");
        }

        log.info("Job Execution: " + jobExecution.getStatus());
        log.info("Job getJobId: " + jobExecution.getJobId());
        log.info("Job getExitStatus: " + jobExecution.getExitStatus());
        log.info("Job getJobInstance: " + jobExecution.getJobInstance());
        log.info("Job getStepExecutions: " + jobExecution.getStepExecutions());
        log.info("Job getLastUpdated: " + jobExecution.getLastUpdated());
        log.info("Job getFailureExceptions: " + jobExecution.getFailureExceptions());
    }

}
