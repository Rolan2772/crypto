package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.storage.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.PoloniexTradingRecord;
import com.crypto.trade.poloniex.storage.TimeFrameStorage;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PoloniexStrategyFactory {

    @Autowired
    private PoloniexProperties properties;
    @Autowired
    private TradeStrategyFactory tradeStrategyFactory;

    public List<TimeFrameStorage> createTestStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        String shortBuyName = "test-short-buy";
        Strategy shortBuyStrategy = tradeStrategyFactory.createTestStrategy(new TimeSeries(timeFrameStorage.getCandles()), TradeStrategyFactory.DEFAULT_TIME_FRAME);
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName, shortBuyStrategy, timeFrame, properties.getTradeConfig().getMinBtcTradeAmount());
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(1, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(2, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(4, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(5, shortBuyName, new TradingRecord()));
        timeFrameStorage.addStrategy(poloniexStrategy);
        return Collections.singletonList(timeFrameStorage);
    }

    public List<TimeFrameStorage> createShortBuyStrategy(CurrencyPair currencyPair) {
        return Arrays.stream(TimeFrame.values()).map(timeFrame -> {
            TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
            String shortBuyName = "short-buy";
            Strategy shortBuyStrategy = tradeStrategyFactory.createShortBuyStrategy(new TimeSeries(timeFrameStorage.getCandles()), TradeStrategyFactory.DEFAULT_TIME_FRAME);
            PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName, shortBuyStrategy, timeFrame, properties.getTradeConfig().getMinBtcTradeAmount());
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(1, shortBuyName, new TradingRecord()));
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(2, shortBuyName, new TradingRecord()));
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, new TradingRecord()));
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(4, shortBuyName, new TradingRecord()));
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(5, shortBuyName, new TradingRecord()));
            timeFrameStorage.addStrategy(poloniexStrategy);
            return timeFrameStorage;
        }).collect(Collectors.toList());
    }

    public List<TimeFrameStorage> createSimpleShortBuyStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        String shortBuyName = "short-buy";
        Strategy shortBuyStrategy = tradeStrategyFactory.createShortBuyStrategy(new TimeSeries(timeFrameStorage.getCandles()), TradeStrategyFactory.DEFAULT_TIME_FRAME);
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName, shortBuyStrategy, timeFrame, properties.getTradeConfig().getMinBtcTradeAmount());
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(1, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(2, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(4, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(5, shortBuyName, new TradingRecord()));
        timeFrameStorage.addStrategy(poloniexStrategy);
        return Collections.singletonList(timeFrameStorage);

    }

    public List<TimeFrameStorage> createRealAmountStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        String shortBuyName = "real-amount-shot-buy";
        Strategy shortBuyStrategy = tradeStrategyFactory.createShortBuyStrategy(new TimeSeries(timeFrameStorage.getCandles()), TradeStrategyFactory.DEFAULT_TIME_FRAME);
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName, shortBuyStrategy, timeFrame, properties.getTradeConfig().getRealBtcTradeAmount());
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(1, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(2, shortBuyName, new TradingRecord()));
        timeFrameStorage.addStrategy(poloniexStrategy);
        return Collections.singletonList(timeFrameStorage);
    }
}
