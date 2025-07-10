package com.guardianes.shared.infrastructure.config;

import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration for asynchronous processing in the application. Provides thread pools for different
 * types of async operations.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    /** Thread pool for audit logging operations */
    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Audit-");
        executor.setRejectedExecutionHandler(
                (r, executor1) -> {
                    logger.warn("Audit task rejected, queue is full. Task: {}", r.toString());
                });
        executor.initialize();
        return executor;
    }

    /** Thread pool for step processing operations */
    @Bean(name = "stepProcessingExecutor")
    public Executor stepProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("StepProcessing-");
        executor.setRejectedExecutionHandler(
                (r, executor1) -> {
                    logger.warn(
                            "Step processing task rejected, queue is full. Task: {}", r.toString());
                });
        executor.initialize();
        return executor;
    }

    /** Thread pool for general async operations */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Async-");
        executor.setRejectedExecutionHandler(
                (r, executor1) -> {
                    logger.warn(
                            "General async task rejected, queue is full. Task: {}", r.toString());
                });
        executor.initialize();
        return executor;
    }
}
