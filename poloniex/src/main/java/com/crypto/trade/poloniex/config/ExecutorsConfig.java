package com.crypto.trade.poloniex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorsConfig {

    @Bean
    public ThreadPoolTaskExecutor tradesExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // @TODO: poll sohuld be smaller, how ever core pull size of 10 threads leads to race conditions
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(200);
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor strategyExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // @TODO: poll sohuld be smaller, how ever core pull size of 10 threads leads to race conditions
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(100);
        return executor;
    }
}
