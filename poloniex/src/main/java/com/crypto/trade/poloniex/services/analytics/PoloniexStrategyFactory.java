package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.storage.*;
import eu.verdelhan.ta4j.Decimal;
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

        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(1, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(2, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(4, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(5, shortBuyName, new TradingRecord()));
        timeFrameStorage.addStrategy(poloniexStrategy);
        return Collections.singletonList(timeFrameStorage);
    }

    public List<TimeFrameStorage> createSimpleRisingTrendStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        String shortBuyName = "rising-trend";
        Strategy shortBuyStrategy = tradeStrategyFactory.createRisingTrendStrategy(new TimeSeries(timeFrameStorage.getCandles()));
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName, shortBuyStrategy, timeFrame, properties.getTradeConfig().getMinBtcTradeAmount());

        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(1, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(2, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(4, shortBuyName, new TradingRecord()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(5, shortBuyName, new TradingRecord()));
        timeFrameStorage.addStrategy(poloniexStrategy);
        return Collections.singletonList(timeFrameStorage);
    }

    public List<TimeFrameStorage> createTestProfitCalculationsStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        String shortBuyName = "profit-test-short-buy-1";
        Strategy shortBuyStrategy = tradeStrategyFactory.createShortBuyStrategy(new TimeSeries(timeFrameStorage.getCandles()), TradeStrategyFactory.DEFAULT_TIME_FRAME);
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName, shortBuyStrategy, timeFrame, BigDecimal.valueOf(0.0001));

        List<ExportedPoloniexOrder> exportedOrders = Stream.of(
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323348216260L, 0, "0.0790100000", "0.00379698", TradingAction.ENTERED),
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323446507870L, 0, "0.0798445800", "0.00378749", TradingAction.EXITED))
                .collect(Collectors.toList());
        PoloniexTradingRecord poloniexTradingRecord = createTratingRecordWithOrders(1, shortBuyName, exportedOrders);
        List<ExportedPoloniexOrder> exportedOrders1 = Stream.of(
                new ExportedPoloniexOrder(shortBuyName + "-tr-2", 323348216260L, 0, "0.0790100000", "0.00379698", TradingAction.ENTERED),
                new ExportedPoloniexOrder(shortBuyName + "-tr-2", 323446507870L, 0, "0.0798445800", "0.00378749", TradingAction.EXITED),
                new ExportedPoloniexOrder(shortBuyName + "-tr-2", 323348216260L, 0, "0.0790100000", "0.00379698", TradingAction.ENTERED),
                new ExportedPoloniexOrder(shortBuyName + "-tr-2", 323446507870L, 0, "0.0798445800", "0.00378749", TradingAction.EXITED))
                .collect(Collectors.toList());
        PoloniexTradingRecord poloniexTradingRecord1 = createTratingRecordWithOrders(2, shortBuyName, exportedOrders1);

        poloniexStrategy.addTradingRecord(poloniexTradingRecord);
        poloniexStrategy.addTradingRecord(poloniexTradingRecord1);
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, new TradingRecord()));
        timeFrameStorage.addStrategy(poloniexStrategy);

        String shortBuyName1 = "profit-test-short-buy-2";
        Strategy shortBuyStrategy1 = tradeStrategyFactory.createShortBuyStrategy(new TimeSeries(timeFrameStorage.getCandles()), TradeStrategyFactory.DEFAULT_TIME_FRAME);
        PoloniexStrategy poloniexStrategy1 = new PoloniexStrategy(shortBuyName1, shortBuyStrategy1, timeFrame, BigDecimal.valueOf(0.0002));

        List<ExportedPoloniexOrder> exportedOrders2 = Stream.of(
                new ExportedPoloniexOrder(shortBuyName1 + "-tr-1", 323348216260L, 0, "0.0790100000", "0.00379698", TradingAction.ENTERED),
                new ExportedPoloniexOrder(shortBuyName1 + "-tr-1", 323446507870L, 0, "0.0798445800", "0.00378749", TradingAction.EXITED),
                new ExportedPoloniexOrder(shortBuyName1 + "-tr-1", 323348216260L, 0, "0.0790100000", "0.00379698", TradingAction.ENTERED),
                new ExportedPoloniexOrder(shortBuyName1 + "-tr-1", 323446507870L, 0, "0.0798445800", "0.00378749", TradingAction.EXITED))
                .collect(Collectors.toList());
        PoloniexTradingRecord poloniexTradingRecord2 = createTratingRecordWithOrders(1, shortBuyName1, exportedOrders2);

        poloniexStrategy1.addTradingRecord(poloniexTradingRecord2);
        timeFrameStorage.addStrategy(poloniexStrategy1);

        return Collections.singletonList(timeFrameStorage);
    }

    public List<TimeFrameStorage> createSimpleShortBuyStrategyWithInitialOrders(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        String shortBuyName = "initial-orders-short-buy";
        Strategy shortBuyStrategy = tradeStrategyFactory.createShortBuyStrategy(new TimeSeries(timeFrameStorage.getCandles()), TradeStrategyFactory.DEFAULT_TIME_FRAME);
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName, shortBuyStrategy, timeFrame, properties.getTradeConfig().getMinBtcTradeAmount());

        List<ExportedPoloniexOrder> exportedOrders = Stream.of(
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323348216260L, 0, "0.08150600", "0.00245380", TradingAction.ENTERED),
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323446507870L, 0, "0.08274000", "0.00244766", TradingAction.EXITED),
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323446507871L, 0, "0.08050011", "0.00130434", TradingAction.ENTERED))
                .collect(Collectors.toList());
        List<ExportedPoloniexOrder> exportedOrders1 = Stream.of(
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323446507872L, 0, "0.08050011", "0.00130434", TradingAction.ENTERED))
                .collect(Collectors.toList());
        PoloniexTradingRecord poloniexTradingRecord = createTratingRecordWithOrders(1, shortBuyName, exportedOrders);
        PoloniexTradingRecord poloniexTradingRecord1 = createTratingRecordWithOrders(2, shortBuyName, exportedOrders1);

        poloniexStrategy.addTradingRecord(poloniexTradingRecord);
        poloniexStrategy.addTradingRecord(poloniexTradingRecord1);
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, new TradingRecord()));
        timeFrameStorage.addStrategy(poloniexStrategy);
        return Collections.singletonList(timeFrameStorage);
    }

    private PoloniexTradingRecord createTratingRecordWithOrders(int id, String shortBuyName, List<ExportedPoloniexOrder> exportedOrders) {
        TradingRecord tradingRecord = new TradingRecord();
        PoloniexTradingRecord poloniexTradingRecord = new PoloniexTradingRecord(id, shortBuyName, tradingRecord);
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
