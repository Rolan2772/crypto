package com.crypto.trade.polonex.config;

import com.crypto.trade.polonex.services.WampConnector;
import com.crypto.trade.polonex.storage.TickersStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
