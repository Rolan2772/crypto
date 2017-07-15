package com.crypto.trade.poloniex.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "api.secret")
public class ApiSecretProperties {

    private String key;
    private String signature;
}
