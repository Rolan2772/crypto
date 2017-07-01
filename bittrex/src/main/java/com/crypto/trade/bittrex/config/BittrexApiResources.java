package com.crypto.trade.bittrex.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "bittrex.resources")
public class BittrexApiResources {

    private String baseUrl;
    private String currenciesUrl;
    private String marketsUrl;
}
