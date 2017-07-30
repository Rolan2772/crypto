package com.crypto.trade.poloniex.config;

import com.crypto.trade.poloniex.services.ws.PlainWsConnector;
import com.crypto.trade.poloniex.services.ws.WsConnectionHandler;
import com.crypto.trade.poloniex.services.ws.WsConnector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Profile("!proxy")
@Configuration
public class NonProxyProfileConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public WsConnector wsConnector() {
        return new PlainWsConnector();
    }

    @Bean
    public WsConnectionHandler wsConnectionHandler() {
        return new WsConnectionHandler();
    }
}
