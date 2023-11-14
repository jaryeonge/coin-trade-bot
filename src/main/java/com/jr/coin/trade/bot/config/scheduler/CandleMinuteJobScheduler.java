package com.jr.coin.trade.bot.config.scheduler;

import com.jr.coin.trade.bot.util.JobUtils;
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
public class CandleMinuteJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job getCandleMinuteBatchJob;

    public CandleMinuteJobScheduler(JobLauncher jobLauncher, Job getCandleMinuteBatchJob) {
        this.jobLauncher = jobLauncher;
        this.getCandleMinuteBatchJob = getCandleMinuteBatchJob;
    }

    @Scheduled(cron = "0 * * * * *")
    public void minuteBTCJobScheduled() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters minuteParameters = new JobParametersBuilder()
                .addString("market", "KRW-BTC")
                .addLong("unit", 1L)
                .addDate("date", new Date())
                .toJobParameters();

        JobExecution minuteJobExecution = jobLauncher.run(getCandleMinuteBatchJob, minuteParameters);

        JobUtils.logJobExecutionDetails(minuteJobExecution);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void hourBTCJobScheduled() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters hourParameters = new JobParametersBuilder()
                .addString("market", "KRW-BTC")
                .addLong("unit", 60L)
                .addDate("date", new Date())
                .toJobParameters();

        JobExecution hourJobExecution = jobLauncher.run(getCandleMinuteBatchJob, hourParameters);

        JobUtils.logJobExecutionDetails(hourJobExecution);
    }


}
