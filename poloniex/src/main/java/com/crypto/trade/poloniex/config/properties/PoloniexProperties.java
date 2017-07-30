package com.crypto.trade.poloniex.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "poloniex")
public class PoloniexProperties {

    private ApiResources api;
    private ApiSecretProperties secret;
    private ProxyProperties proxy;
    private TradeConfig tradeConfig;
}
