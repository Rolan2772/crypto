package com.crypto.trade.polonex.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
public class ApiResources {

    private String wsUrl;
}
