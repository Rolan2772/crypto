package com.crypto.trade.poloniex.services.bots;

import com.crypto.trade.poloniex.services.connection.WsConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class SimpleTradingBot {

    @Autowired
    private WsConnector wampWsConnector;

    @PostConstruct
    public void postConstruct() {
        wampWsConnector.connect();
    }

    @PreDestroy
    public void preDestroy() {
        wampWsConnector.closeConnection();
    }
}
