package com.crypto.trade.poloniex.services.bots;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.PoloniexStrategyFactory;
import com.crypto.trade.poloniex.services.trade.HistoryService;
import com.crypto.trade.poloniex.services.ws.WsConnector;
import com.crypto.trade.poloniex.storage.CandlesStorage;
import com.crypto.trade.poloniex.storage.TradesStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.websocket.DeploymentException;
import java.io.IOException;
import java.time.Duration;

@Service
public class SimplePoloniexBot {

    @Qualifier("fileTradesHistoryService")
//    @Qualifier("copyTradesHistoryService")
    @Autowired
    private HistoryService historyService;
    @Autowired
    private TradesStorage tradesStorage;
    @Autowired
    private WsConnector wsConnector;
    @Autowired
    private PoloniexStrategyFactory poloniexStrategyFactory;
    @Autowired
    private CandlesStorage candlesStorage;

    @PostConstruct
    public void postConstruct() throws IOException, DeploymentException {
        CurrencyPair btcEth = CurrencyPair.BTC_ETH;
        tradesStorage.initCurrency(btcEth);
        candlesStorage.initCurrency(btcEth, poloniexStrategyFactory.experimentOneStrategy(btcEth));
        //wsConnector.connect();
        tradesStorage.addTradesHistory(btcEth, historyService.loadTradesHistory(btcEth, Duration.ofHours(6)));
    }

    @PreDestroy
    public void preDestroy() {
        wsConnector.closeConnection();
    }
}
