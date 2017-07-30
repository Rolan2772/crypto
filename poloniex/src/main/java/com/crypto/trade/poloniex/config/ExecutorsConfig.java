package com.crypto.trade.poloniex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ExecutorsConfig {

    @Bean
    public ThreadPoolTaskExecutor tradesExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // @TODO: pool should be smaller, how ever core pull size of 10 threads leads to race conditions
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(50);
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor strategyExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // @TODO: pool should be smaller, how ever core pull size of 10 threads leads to race conditions
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(50);
        return executor;
    }

    @Bean
    public ThreadPoolTaskScheduler connectionScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        return scheduler;
    }
}
