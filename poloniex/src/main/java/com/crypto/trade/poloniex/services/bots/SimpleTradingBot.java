package com.crypto.trade.poloniex.services.bots;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.integration.LoadHistoryService;
import com.crypto.trade.poloniex.services.integration.ws.WsConnector;
import com.crypto.trade.poloniex.storage.TradesStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.websocket.DeploymentException;
import java.io.IOException;

@Service
public class SimpleTradingBot {

    @Autowired
    private LoadHistoryService loadHistoryService;
    @Autowired
    private TradesStorage tradesStorage;
    @Qualifier("tyrusWsConnector")
    @Autowired
    private WsConnector wsConnector;

    @PostConstruct
    public void postConstruct() throws IOException, DeploymentException {

        wsConnector.connect();
        tradesStorage.addTradesHistory(CurrencyPair.BTC_ETH, loadHistoryService.loadTradesHistory());
    }

    @PreDestroy
    public void preDestroy() {
        wsConnector.closeConnection();
    }
}
