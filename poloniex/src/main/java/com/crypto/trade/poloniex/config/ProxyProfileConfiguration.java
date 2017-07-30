package com.crypto.trade.poloniex.config;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.services.ws.PoloniexEndPoint;
import com.crypto.trade.poloniex.services.ws.TyrusWsConnector;
import com.crypto.trade.poloniex.services.ws.WsConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Profile("proxy")
@Configuration
public class ProxyProfileConfiguration {

    @Autowired
    private PoloniexProperties properties;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(properties.getProxy().getHost(), properties.getProxy().getPort()));
        requestFactory.setProxy(proxy);

        return new RestTemplate(requestFactory);
    }

    @Bean
    public WsConnector wsConnector() {
        return new TyrusWsConnector();
    }

    @Bean
    public PoloniexEndPoint poloniexEndPoint() {
        return new PoloniexEndPoint();
    }
}
