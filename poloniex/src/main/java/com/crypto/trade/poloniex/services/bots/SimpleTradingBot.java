package com.crypto.trade.poloniex.services.bots;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.StrategiesBuilder;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.integration.LoadHistoryService;
import com.crypto.trade.poloniex.services.integration.ws.WsConnector;
import com.crypto.trade.poloniex.storage.*;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.websocket.DeploymentException;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SimpleTradingBot {

    @Autowired
    private LoadHistoryService loadHistoryService;
    @Autowired
    private CandlesStorage candlesStorage;
    @Autowired
    private StrategiesBuilder strategiesBuilder;

    @Autowired
    private TradesStorage tradesStorage;
    @Qualifier("tyrusWsConnector")
    @Autowired
    private WsConnector wsConnector;

    @PostConstruct
    public void postConstruct() throws IOException, DeploymentException {
        tradesStorage.initCurrency(CurrencyPair.BTC_ETH);
        buildTradingStrategies(CurrencyPair.BTC_ETH);
        //buildTestTradingStrategy(CurrencyPair.BTC_ETH);
        wsConnector.connect();
        tradesStorage.addTradesHistory(CurrencyPair.BTC_ETH, loadHistoryService.loadTradesHistory(CurrencyPair.BTC_ETH, Duration.ofMinutes(20)));
    }

    private void buildTestTradingStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        String shortBuyName = "testStrategy";
        Strategy shortBuyStrategy = strategiesBuilder.buildTestStrategy(new TimeSeries(timeFrameStorage.getCandles()), StrategiesBuilder.DEFAULT_TIME_FRAME);
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName, shortBuyStrategy, timeFrame);
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(1, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(2, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(4, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(5, shortBuyName, new TradingRecord()));
        timeFrameStorage.addStrategy(poloniexStrategy);
        candlesStorage.setupCurrency(currencyPair, Collections.singletonList(timeFrameStorage));
    }

    private void buildTradingStrategies(CurrencyPair currencyPair) {
        List<TimeFrameStorage> timeFrameData = Arrays.stream(TimeFrame.values()).map(timeFrame -> {
            TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
            String shortBuyName = "shortBuy";
            Strategy shortBuyStrategy = strategiesBuilder.buildShortBuyStrategy(new TimeSeries(timeFrameStorage.getCandles()), StrategiesBuilder.DEFAULT_TIME_FRAME);
            PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName, shortBuyStrategy, timeFrame);
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(1, shortBuyName, new TradingRecord()));
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(2, shortBuyName, new TradingRecord()));
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, new TradingRecord()));
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(4, shortBuyName, new TradingRecord()));
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(5, shortBuyName, new TradingRecord()));
            timeFrameStorage.addStrategy(poloniexStrategy);
            return timeFrameStorage;
        }).collect(Collectors.toList());
        candlesStorage.setupCurrency(currencyPair, timeFrameData);
    }

    @PreDestroy
    public void preDestroy() {
        wsConnector.closeConnection();
    }
}
