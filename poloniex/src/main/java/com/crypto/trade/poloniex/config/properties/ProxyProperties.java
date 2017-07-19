package com.crypto.trade.poloniex.config.properties;

import lombok.Data;

@Data
public class ProxyProperties {

    private String host;
    private Integer port;

    public String getUri() {
        return "http://" + host + ":" + port;
    }
}
