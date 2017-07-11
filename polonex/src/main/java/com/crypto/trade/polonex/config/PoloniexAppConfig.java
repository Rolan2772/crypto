package com.crypto.trade.polonex.config;

import com.crypto.trade.polonex.config.properties.PoloniexProperties;
import com.crypto.trade.polonex.storage.HistoryStorage;
import com.crypto.trade.polonex.storage.TickersStorage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(PoloniexProperties.class)
public class PoloniexAppConfig {

    @Bean
    public TickersStorage tickersStorage() {
        return new TickersStorage();
    }

    @Bean
    public HistoryStorage historyStorage() {
        return new HistoryStorage();
    }
}
