package com.crypto.trade.poloniex.config;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.services.analytics.StrategiesBuilder;
import com.crypto.trade.poloniex.services.integration.ws.PoloniexEndPoint;
import com.crypto.trade.poloniex.services.integration.PoloniexRequestHelper;
import com.crypto.trade.poloniex.services.integration.ws.WsConnectionHandler;
import com.crypto.trade.poloniex.storage.HistoryStorage;
import com.crypto.trade.poloniex.storage.TickersStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({PoloniexProperties.class})
public class PoloniexAppConfig {

    @Autowired
    private PoloniexProperties properties;

    @Bean
    public TickersStorage tickersStorage() {
        return new TickersStorage();
    }

    @Bean
    public HistoryStorage historyStorage() {
        return new HistoryStorage();
    }

    @Bean
    public StrategiesBuilder strategiesBuilder() {
        return new StrategiesBuilder();
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        Proxy proxy= new Proxy(Proxy.Type.HTTP, new InetSocketAddress(properties.getProxy().getHost(), properties.getProxy().getPort()));
        requestFactory.setProxy(proxy);

        return new RestTemplate(requestFactory);
    }

    @Bean
    public WsConnectionHandler wsConnectionHandler() {
        return new WsConnectionHandler();
    }

    @Bean
    public PoloniexRequestHelper poloniexRequestUtils() {
        return new PoloniexRequestHelper();
    }

    @Bean
    public PoloniexEndPoint poloniexEndPoint() {
        return new PoloniexEndPoint();
    }
}
