package com.jr.coin.trade.bot.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;

import java.time.LocalDateTime;

@Slf4j
public class JobUtils {

    public static void logJobExecutionDetails(JobExecution jobExecution) {
        log.info("Job Execution: " + jobExecution.getStatus());
        log.info("Job getJobId: " + jobExecution.getJobId());
        log.info("Job getExitStatus: " + jobExecution.getExitStatus());
        log.info("Job getJobInstance: " + jobExecution.getJobInstance());
        log.info("Job getStepExecutions: " + jobExecution.getStepExecutions());
        log.info("Job getLastUpdated: " + jobExecution.getLastUpdated());
        log.info("Job getFailureExceptions: " + jobExecution.getFailureExceptions());
    }

    public static boolean checkSchedule(ScheduleCode code) {
        switch (code) {
            case MINUTE: return true;
            case HOUR:
                if (LocalDateTime.now().getMinute() == 0) {
                    return true;
                }
            case DAY:
                if (LocalDateTime.now().getHour() == 0 && LocalDateTime.now().getMinute() == 0) {
                    return true;
                }
            default: return false;
        }
    }

}
