package com.crypto.trade.poloniex.services.bots;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.StrategiesBuilder;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.trade.LoadHistoryService;
import com.crypto.trade.poloniex.services.ws.WsConnector;
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
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SimpleTradingBot {

    // Minimal order amount
    public static final BigDecimal BTC_MIN_TRADE_AMOUNT = new BigDecimal("0.000105");
    // 50$ approximately
    public static final BigDecimal BTC_REAL_TRADE_AMOUNT = new BigDecimal("0.018");

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
        buildRsiSlowFastStrategy(CurrencyPair.BTC_ETH);
        wsConnector.connect();
        tradesStorage.addTradesHistory(CurrencyPair.BTC_ETH, loadHistoryService.loadTradesHistory(CurrencyPair.BTC_ETH, Duration.ofHours(4)));
    }

    //!!!!!!!!!!!!!!!!!!!!!!!!!!! real flag should be false
    private void buildTestTradingStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        String shortBuyName = "test-short-buy";
        Strategy shortBuyStrategy = strategiesBuilder.buildTestStrategy(new TimeSeries(timeFrameStorage.getCandles()), StrategiesBuilder.DEFAULT_TIME_FRAME);
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName, shortBuyStrategy, timeFrame, BTC_MIN_TRADE_AMOUNT);
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(1, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(2, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(4, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(5, shortBuyName, new TradingRecord()));
        timeFrameStorage.addStrategy(poloniexStrategy);
        candlesStorage.setupCurrency(currencyPair, Collections.singletonList(timeFrameStorage));
    }

    private void buildShortBuyAllTimeFrames(CurrencyPair currencyPair) {
        List<TimeFrameStorage> timeFrameData = Arrays.stream(TimeFrame.values()).map(timeFrame -> {
            TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
            String shortBuyName = "short-buy";
            Strategy shortBuyStrategy = strategiesBuilder.buildShortBuyStrategy(new TimeSeries(timeFrameStorage.getCandles()), StrategiesBuilder.DEFAULT_TIME_FRAME);
            PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName, shortBuyStrategy, timeFrame, BTC_MIN_TRADE_AMOUNT);
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

    private void buildRealTradingAmountBuyStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        String shortBuyName = "real-amount-shot-buy";
        Strategy shortBuyStrategy = strategiesBuilder.buildShortBuyStrategy(new TimeSeries(timeFrameStorage.getCandles()), StrategiesBuilder.DEFAULT_TIME_FRAME);
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName, shortBuyStrategy, timeFrame, BTC_REAL_TRADE_AMOUNT);
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(1, shortBuyName, new TradingRecord()));
        timeFrameStorage.addStrategy(poloniexStrategy);
        candlesStorage.setupCurrency(currencyPair, Collections.singletonList(timeFrameStorage));
    }

    private void buildRsiSlowFastStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        String shortBuyName = "rsi-slow-fast";
        Strategy shortBuyStrategy = strategiesBuilder.buildRsiSlowFastStrategy(new TimeSeries(timeFrameStorage.getCandles()));
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName, shortBuyStrategy, timeFrame, BTC_MIN_TRADE_AMOUNT);
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(1, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(2, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(4, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(5, shortBuyName, new TradingRecord()));
        timeFrameStorage.addStrategy(poloniexStrategy);
        candlesStorage.setupCurrency(currencyPair, Collections.singletonList(timeFrameStorage));
    }

    @PreDestroy
    public void preDestroy() {
        wsConnector.closeConnection();
    }
}
