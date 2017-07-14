package com.crypto.trade.poloniex.config;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.services.analytics.StrategiesBuilder;
import com.crypto.trade.poloniex.services.connection.WsConnectionHandler;
import com.crypto.trade.poloniex.storage.HistoryStorage;
import com.crypto.trade.poloniex.storage.TickersStorage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

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

    @Bean
    public StrategiesBuilder strategiesBuilder() {
        return new StrategiesBuilder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public WsConnectionHandler wsConnectionHandler() {
        return new WsConnectionHandler();
    }
}
