package com.crypto.trade.polonex.config;

import com.crypto.trade.polonex.services.WampConnector;
import com.crypto.trade.polonex.storage.TickersStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class PolonexAppConfig {

    @Bean
    public WampConnector wampConnector() {
        return new WampConnector();
    }

    @Bean
    public TickersStorage tickersStorage() {
        return new TickersStorage();
    }
}
