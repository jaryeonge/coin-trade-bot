package com.jr.coin.trade.bot.config.scheduler;

import com.jr.coin.trade.bot.util.JobUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class AccountJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job getAccountBatchJob;

    public AccountJobScheduler(JobLauncher jobLauncher, Job getAccountBatchJob) {
        this.jobLauncher = jobLauncher;
        this.getAccountBatchJob = getAccountBatchJob;
    }

    @Scheduled(cron = "0 * * * * *")
    public void jobScheduled() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters parameters = new JobParameters();

        JobExecution jobExecution = jobLauncher.run(getAccountBatchJob, parameters);
        JobUtils.logJobExecutionDetails(jobExecution);
    }
}
