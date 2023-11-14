package com.jr.coin.trade.bot.config.batch;

import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableBatchProcessing
public class BatchConfig extends DefaultBatchConfiguration {

    private final static int CORE_POOL_SIZE = 15;
    private final static int MAX_POOL_SIZE = 20;
    private final static int QUEUE_CAPACITY = 30;

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(CORE_POOL_SIZE);
        taskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        taskExecutor.setQueueCapacity(QUEUE_CAPACITY);
        return taskExecutor;
    }

    @Override
    @Bean
    public JobLauncher jobLauncher() throws BatchConfigurationException {
        TaskExecutorJobLauncher taskExecutorJobLauncher = new TaskExecutorJobLauncher();
        taskExecutorJobLauncher.setJobRepository(jobRepository());
        taskExecutorJobLauncher.setTaskExecutor(taskExecutor());
        try {
            taskExecutorJobLauncher.afterPropertiesSet();
            return taskExecutorJobLauncher;
        }
        catch (Exception e) {
            throw new BatchConfigurationException("Unable to configure the default job launcher", e);
        }
    }

}
