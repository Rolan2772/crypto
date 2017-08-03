package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.storage.*;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        String shortBuyName = "new-short-buy";
        Strategy shortBuyStrategy = tradeStrategyFactory.createShortBuyStrategy(new TimeSeries(timeFrameStorage.getCandles()), TradeStrategyFactory.DEFAULT_TIME_FRAME);
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName, shortBuyStrategy, timeFrame, properties.getTradeConfig().getMinBtcTradeAmount());

        List<ExportedPoloniexOrder> exportedOrders = Stream.of(
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323348216260L, 0, "0.08150600", "0.00245380", TradingAction.ENTERED),
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323446507870L, 0, "0.08274000", "0.00244766", TradingAction.EXITED),
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323446507871L, 0, "0.08170600", "0.00245380", TradingAction.ENTERED),
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323446507872L, 0, "0.08299000", "0.00244766", TradingAction.EXITED))
                .collect(Collectors.toList());
        PoloniexTradingRecord poloniexTradingRecord = createTratingRecordWithOrders(shortBuyName, exportedOrders);

        poloniexStrategy.addTradingRecord(poloniexTradingRecord);
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(2, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, new TradingRecord()));
        timeFrameStorage.addStrategy(poloniexStrategy);
        return Collections.singletonList(timeFrameStorage);
    }

    private PoloniexTradingRecord createTratingRecordWithOrders(String shortBuyName, List<ExportedPoloniexOrder> exportedOrders) {
        PoloniexTradingRecord poloniexTradingRecord = new PoloniexTradingRecord(1, shortBuyName, new TradingRecord());
        TradingRecord tradingRecord = new TradingRecord();
        exportedOrders.forEach(exportedOrder -> {
            if (TradingAction.ENTERED == exportedOrder.getTradingAction()) {
                tradingRecord.enter(exportedOrder.getIndex(), Decimal.valueOf(exportedOrder.getRate()), Decimal.valueOf(exportedOrder.getAmount()));
            } else {
                tradingRecord.exit(exportedOrder.getIndex(), Decimal.valueOf(exportedOrder.getRate()), Decimal.valueOf(exportedOrder.getAmount()));
            }
            PoloniexOrder poloniexOrder = new PoloniexOrder(exportedOrder.getOrderId(), tradingRecord.getLastOrder(), exportedOrder.getIndex(), exportedOrder.getTradingAction());
            poloniexTradingRecord.addPoloniexOrder(poloniexOrder);
        });
        return poloniexTradingRecord;
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
