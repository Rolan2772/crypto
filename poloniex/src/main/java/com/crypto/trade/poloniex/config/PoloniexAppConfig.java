package com.crypto.trade.poloniex.config;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.services.analytics.poloniex.ExperimentalTradeConfigFactory;
import com.crypto.trade.poloniex.services.analytics.poloniex.RealTradeConfFactory;
import com.crypto.trade.poloniex.services.analytics.strategies.AnalyticsHelper;
import com.crypto.trade.poloniex.services.analytics.strategies.ShortBuyStrategyFactory;
import com.crypto.trade.poloniex.services.analytics.strategies.TmaStrategyFactory;
import com.crypto.trade.poloniex.services.analytics.strategies.TrendStrategyFactory;
import com.crypto.trade.poloniex.services.export.ExportHelper;
import com.crypto.trade.poloniex.services.trade.PoloniexRequestHelper;
import com.crypto.trade.poloniex.services.trade.ProfitCalculator;
import com.crypto.trade.poloniex.services.trade.SignatureGenerator;
import com.crypto.trade.poloniex.services.utils.SyncUtils;
import com.crypto.trade.poloniex.storage.CandlesStorage;
import com.crypto.trade.poloniex.storage.TradesStorage;
import com.crypto.trade.poloniex.storage.analytics.AnalyticsStorage;
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
    public ShortBuyStrategyFactory shortBuyFactory() {
        return new ShortBuyStrategyFactory();
    }

    @Bean
    public TrendStrategyFactory trendFactory() {
        return new TrendStrategyFactory();
    }

    @Bean
    public TmaStrategyFactory tmaFactory() {
        return new TmaStrategyFactory();
    }

    @Bean
    public AnalyticsHelper indicatorsHelper() {
        return new AnalyticsHelper();
    }

    @Bean
    public ExperimentalTradeConfigFactory experimentalTradeConfigFactory() {
        return new ExperimentalTradeConfigFactory();
    }

    @Bean
    public RealTradeConfFactory realTradeConfFactory() {
        return new RealTradeConfFactory();
    }

    @Bean
    public PoloniexRequestHelper poloniexRequestUtils() {
        return new PoloniexRequestHelper();
    }

    @Bean
    public SyncUtils syncUtils() {
        return new SyncUtils();
    }

    @Bean
    public ExportHelper exportHelper() {
        return new ExportHelper();
    }

    @Bean
    public ProfitCalculator profitCalculator() {
        return new ProfitCalculator();
    }

    @Bean
    public AnalyticsStorage analyticsCache() {
        return new AnalyticsStorage();
    }

    @Bean
    public SignatureGenerator signatureGenerator() {
        return new SignatureGenerator();
    }
}
