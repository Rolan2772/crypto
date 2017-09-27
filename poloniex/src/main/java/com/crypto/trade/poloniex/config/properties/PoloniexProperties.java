package com.crypto.trade.poloniex.config.properties;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Data
@ConfigurationProperties(prefix = "poloniex")
public class PoloniexProperties {

    private ApiResources api;
    private ApiSecretProperties secret;
    private ProxyProperties proxy;
    private TradeConfig tradeConfig;
    private Set<CurrencyPair> currencies;
}
