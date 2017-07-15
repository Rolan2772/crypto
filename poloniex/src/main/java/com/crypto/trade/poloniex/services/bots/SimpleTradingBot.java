package com.crypto.trade.poloniex.services.bots;

import com.crypto.trade.poloniex.services.integration.WsConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class SimpleTradingBot {

    @Autowired
    private WsConnector plainWsConnector;

    @PostConstruct
    public void postConstruct() {
        plainWsConnector.connect();
    }

    @PreDestroy
    public void preDestroy() {
        plainWsConnector.closeConnection();
    }
}
