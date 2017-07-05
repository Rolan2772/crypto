package com.crypto.trade.polonex.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "poloniex")
public class PoloniexProperties {

    private ApiResources apiResources;
}
