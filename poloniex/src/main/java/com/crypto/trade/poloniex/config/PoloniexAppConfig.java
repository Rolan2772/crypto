package com.crypto.trade.poloniex.config;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.services.analytics.PoloniexStrategyFactory;
import com.crypto.trade.poloniex.services.analytics.TradeStrategyFactory;
import com.crypto.trade.poloniex.services.export.ExportHelper;
import com.crypto.trade.poloniex.services.trade.PoloniexRequestHelper;
import com.crypto.trade.poloniex.storage.CandlesStorage;
import com.crypto.trade.poloniex.storage.TradesStorage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({PoloniexProperties.class})
public class PoloniexAppConfig {

    @Bean
    public TradesStorage tradesStorage() {
        return new TradesStorage();
    }

    @Bean
    public CandlesStorage strategiesStorage() {
        return new CandlesStorage();
    }

    @Bean
    public TradeStrategyFactory tradeStrategyBuilder() {
        return new TradeStrategyFactory();
    }

    @Bean
    public PoloniexStrategyFactory poloniexStrategyBuilder() {
        return new PoloniexStrategyFactory();
    }

    @Bean
    public PoloniexRequestHelper poloniexRequestUtils() {
        return new PoloniexRequestHelper();
    }

    @Bean
    public ExportHelper exportHelper() {
        return new ExportHelper();
    }

}
