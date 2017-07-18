package com.crypto.trade.poloniex.services.bots;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.integration.LoadHistoryService;
import com.crypto.trade.poloniex.services.integration.WsConnector;
import com.crypto.trade.poloniex.storage.TickersStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class SimpleTradingBot {

    @Autowired
    private LoadHistoryService loadHistoryService;
    @Autowired
    private TickersStorage tickersStorage;

    @Autowired
    private WsConnector plainWsConnector;

    @PostConstruct
    public void postConstruct() {

        //plainWsConnector.connect();
        tickersStorage.addTradesHistory(CurrencyPair.BTC_ETH, loadHistoryService.loadTradesHistory());
    }

    @PreDestroy
    public void preDestroy() {
        plainWsConnector.closeConnection();
    }
}
