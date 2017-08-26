package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.storage.*;
import eu.verdelhan.ta4j.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public class PoloniexStrategyFactory {

    @Autowired
    private PoloniexProperties properties;
    @Autowired
    private TradeStrategyFactory tradeStrategyFactory;

    public List<TimeFrameStorage> createShortBuyStrategy(CurrencyPair currencyPair) {
        return Arrays.stream(TimeFrame.values()).map(timeFrame -> {
            TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
            String shortBuyName = "short-buy";
            Strategy shortBuyStrategy = tradeStrategyFactory.createShortBuyStrategy(timeFrame, new TimeSeries(timeFrameStorage.getCandles()));
            PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName,
                    shortBuyStrategy,
                    timeFrame,
                    Order.OrderType.BUY,
                    properties.getTradeConfig().getMinBtcTradeAmount());
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(1, shortBuyName, poloniexStrategy.getDirection()));
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(2, shortBuyName, poloniexStrategy.getDirection()));
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, poloniexStrategy.getDirection()));
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(4, shortBuyName, poloniexStrategy.getDirection()));
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(5, shortBuyName, poloniexStrategy.getDirection()));
            timeFrameStorage.addStrategy(poloniexStrategy);
            return timeFrameStorage;
        }).collect(Collectors.toList());
    }

    public List<TimeFrameStorage> createSimpleShortBuyStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        String shortBuyName = "new-short-buy";
        Strategy shortBuyStrategy = tradeStrategyFactory.createShortBuyStrategy(timeFrame, new TimeSeries(timeFrameStorage.getCandles()));
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName,
                shortBuyStrategy,
                timeFrame,
                Order.OrderType.BUY,
                properties.getTradeConfig().getMinBtcTradeAmount());
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(1, shortBuyName, poloniexStrategy.getDirection()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(2, shortBuyName, poloniexStrategy.getDirection()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, poloniexStrategy.getDirection()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(4, shortBuyName, poloniexStrategy.getDirection()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(5, shortBuyName, poloniexStrategy.getDirection()));
        timeFrameStorage.addStrategy(poloniexStrategy);
        return Collections.singletonList(timeFrameStorage);
    }

    public List<TimeFrameStorage> createSimpleRisingTrendStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        String shortBuyName = "rising-trend";
        Strategy shortBuyStrategy = tradeStrategyFactory.createRisingTrendStrategy(timeFrame, new TimeSeries(timeFrameStorage.getCandles()));
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName,
                shortBuyStrategy,
                timeFrame,
                Order.OrderType.BUY,
                properties.getTradeConfig().getMinBtcTradeAmount());

        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(1, shortBuyName, poloniexStrategy.getDirection()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(2, shortBuyName, poloniexStrategy.getDirection()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, poloniexStrategy.getDirection()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(4, shortBuyName, poloniexStrategy.getDirection()));
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(5, shortBuyName, poloniexStrategy.getDirection()));
        timeFrameStorage.addStrategy(poloniexStrategy);
        return Collections.singletonList(timeFrameStorage);
    }

    public List<TimeFrameStorage> createTestProfitCalculationsStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        String shortBuyName = "profit-test-short-buy-1";
        Strategy shortBuyStrategy = tradeStrategyFactory.createShortBuyStrategy(timeFrame, new TimeSeries(timeFrameStorage.getCandles()));
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName,
                shortBuyStrategy,
                timeFrame,
                Order.OrderType.BUY,
                BigDecimal.valueOf(0.0001));

        List<ExportedPoloniexOrder> exportedOrders = Stream.of(
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323348216260L, 0, "0.0790100000", "0.00379698", TradingAction.ENTERED),
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323446507870L, 0, "0.0798445800", "0.00378749", TradingAction.EXITED))
                .collect(Collectors.toList());
        PoloniexTradingRecord poloniexTradingRecord = createTradingRecordWithOrders(1, shortBuyName, exportedOrders);
        List<ExportedPoloniexOrder> exportedOrders1 = Stream.of(
                new ExportedPoloniexOrder(shortBuyName + "-tr-2", 323348216260L, 0, "0.0790100000", "0.00379698", TradingAction.ENTERED),
                new ExportedPoloniexOrder(shortBuyName + "-tr-2", 323446507870L, 0, "0.0798445800", "0.00378749", TradingAction.EXITED),
                new ExportedPoloniexOrder(shortBuyName + "-tr-2", 323348216260L, 0, "0.0790100000", "0.00379698", TradingAction.ENTERED),
                new ExportedPoloniexOrder(shortBuyName + "-tr-2", 323446507870L, 0, "0.0798445800", "0.00378749", TradingAction.EXITED))
                .collect(Collectors.toList());
        PoloniexTradingRecord poloniexTradingRecord1 = createTradingRecordWithOrders(2, shortBuyName, exportedOrders1);

        poloniexStrategy.addTradingRecord(poloniexTradingRecord);
        poloniexStrategy.addTradingRecord(poloniexTradingRecord1);
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, Order.OrderType.BUY));
        timeFrameStorage.addStrategy(poloniexStrategy);

        String shortBuyName1 = "profit-test-short-buy-2";
        Strategy shortBuyStrategy1 = tradeStrategyFactory.createShortBuyStrategy(timeFrame, new TimeSeries(timeFrameStorage.getCandles()));
        PoloniexStrategy poloniexStrategy1 = new PoloniexStrategy(shortBuyName1,
                shortBuyStrategy1,
                timeFrame,
                Order.OrderType.BUY,
                BigDecimal.valueOf(0.0002));

        List<ExportedPoloniexOrder> exportedOrders2 = Stream.of(
                new ExportedPoloniexOrder(shortBuyName1 + "-tr-1", 323348216260L, 0, "0.0790100000", "0.00379698", TradingAction.ENTERED),
                new ExportedPoloniexOrder(shortBuyName1 + "-tr-1", 323446507870L, 0, "0.0798445800", "0.00378749", TradingAction.EXITED),
                new ExportedPoloniexOrder(shortBuyName1 + "-tr-1", 323348216260L, 0, "0.0790100000", "0.00379698", TradingAction.ENTERED),
                new ExportedPoloniexOrder(shortBuyName1 + "-tr-1", 323446507870L, 0, "0.0798445800", "0.00378749", TradingAction.EXITED))
                .collect(Collectors.toList());
        PoloniexTradingRecord poloniexTradingRecord2 = createTradingRecordWithOrders(1, shortBuyName1, exportedOrders2);

        poloniexStrategy1.addTradingRecord(poloniexTradingRecord2);
        timeFrameStorage.addStrategy(poloniexStrategy1);

        return Collections.singletonList(timeFrameStorage);
    }

    public List<TimeFrameStorage> createSimpleShortBuyStrategyWithInitialOrders(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        String shortBuyName = "initial-orders-short-buy";
        Strategy shortBuyStrategy = tradeStrategyFactory.createShortBuyStrategy(timeFrame, new TimeSeries(timeFrameStorage.getCandles()));
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(shortBuyName,
                shortBuyStrategy,
                timeFrame,
                Order.OrderType.BUY,
                properties.getTradeConfig().getMinBtcTradeAmount());

        List<ExportedPoloniexOrder> exportedOrders = Stream.of(
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323348216260L, 0, "0.08150600", "0.00245380", TradingAction.ENTERED),
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323446507870L, 0, "0.08274000", "0.00244766", TradingAction.EXITED),
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323446507871L, 0, "0.08050011", "0.00130434", TradingAction.ENTERED))
                .collect(Collectors.toList());
        List<ExportedPoloniexOrder> exportedOrders1 = Stream.of(
                new ExportedPoloniexOrder(shortBuyName + "-tr-1", 323446507872L, 0, "0.08050011", "0.00130434", TradingAction.ENTERED))
                .collect(Collectors.toList());
        PoloniexTradingRecord poloniexTradingRecord = createTradingRecordWithOrders(1, shortBuyName, exportedOrders);
        PoloniexTradingRecord poloniexTradingRecord1 = createTradingRecordWithOrders(2, shortBuyName, exportedOrders1);

        poloniexStrategy.addTradingRecord(poloniexTradingRecord);
        poloniexStrategy.addTradingRecord(poloniexTradingRecord1);
        poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(3, shortBuyName, Order.OrderType.BUY));
        timeFrameStorage.addStrategy(poloniexStrategy);
        return Collections.singletonList(timeFrameStorage);
    }

    private PoloniexTradingRecord createTradingRecordWithOrders(int id, String shortBuyName, List<ExportedPoloniexOrder> exportedOrders) {
        PoloniexTradingRecord poloniexTradingRecord = new PoloniexTradingRecord(id, shortBuyName, Order.OrderType.BUY);
        TradingRecord tradingRecord = poloniexTradingRecord.getTradingRecord();
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

    public List<TimeFrameStorage> createRealAmountBuyConf1(CurrencyPair currencyPair) {
        String strategyName = "real-shot-buy-conf-1";
        return Stream.of(createStrategy(strategyName, TimeFrame.ONE_MINUTE, 10),
                createStrategy(strategyName, TimeFrame.FIVE_MINUTES, 3))
                .collect(Collectors.toList());
    }

    private TimeFrameStorage createStrategy(String strategyName, TimeFrame timeFrame, int recordsCount) {
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        Strategy shortBuyStrategy = tradeStrategyFactory.createShortBuyStrategy(timeFrame, new TimeSeries(timeFrameStorage.getCandles()));
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(strategyName,
                shortBuyStrategy,
                timeFrame,
                Order.OrderType.BUY,
                properties.getTradeConfig().getRealBtcTradeAmount());
        IntStream.rangeClosed(1, recordsCount).forEach(index -> {
            poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(index, strategyName, poloniexStrategy.getDirection()));
        });
        timeFrameStorage.addStrategy(poloniexStrategy);
        return timeFrameStorage;
    }

    public List<TimeFrameStorage> createRealAmountBuyConf2(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        BigDecimal volume = BigDecimal.valueOf(0.08);
        initStrategy("modified2-buy-rising-ema90",
                timeFrameStorage,
                1,
                volume,
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createModifiedShortBuyEma90RisingTrendStrategy2(timeFrame, ticks));
        initStrategy("rising-trend",
                timeFrameStorage,
                1,
                volume,
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createRisingTrendStrategy(timeFrame, ticks));
        initStrategy("tma-strategy-2",
                timeFrameStorage,
                1,
                volume,
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createRisingTripleEmaStrategy2(timeFrame, ticks));
        return Arrays.asList(timeFrameStorage);
    }

    public List<TimeFrameStorage> createTopPerformingStrategies(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        initStrategy("sell-falling-ema90-1",
                timeFrameStorage,
                2,
                BigDecimal.valueOf(0.05),
                Order.OrderType.SELL,
                ticks -> tradeStrategyFactory.createShortSellEma90FallingTrendStrategy1(timeFrame, ticks));
        initStrategy("modified2-buy-rising-ema90",
                timeFrameStorage,
                2,
                BigDecimal.valueOf(0.005),
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createModifiedShortBuyEma90RisingTrendStrategy2(timeFrame, ticks));
        initStrategy("rising-trend",
                timeFrameStorage,
                2,
                BigDecimal.valueOf(0.005),
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createRisingTrendStrategy(timeFrame, ticks));
        initStrategy("tma-strategy-2",
                timeFrameStorage,
                2,
                BigDecimal.valueOf(0.005),
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createRisingTripleEmaStrategy2(timeFrame, ticks));
        return Arrays.asList(timeFrameStorage);
    }

    public List<TimeFrameStorage> experimentOneStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);

        BigDecimal minVolume = properties.getTradeConfig().getMinBtcTradeAmount();
        BigDecimal realVolume = properties.getTradeConfig().getRealBtcTradeAmount();

        initStrategy("short-buy-rising-ema90",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createShortBuyEma90RisingTrendStrategy(timeFrame, ticks));
        initStrategy("modified1-buy-rising-ema90",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createModifiedShortBuyEma90RisingTrendStrategy1(timeFrame, ticks));
        initStrategy("modified2-buy-rising-ema90",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createModifiedShortBuyEma90RisingTrendStrategy2(timeFrame, ticks));
        initStrategy("modified3-buy-rising-ema90",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createModifiedShortBuyEma90RisingTrendStrategy3(timeFrame, ticks));
        initStrategy("modified4-buy-rising-ema90",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createModifiedShortBuyEma90RisingTrendStrategy4(timeFrame, ticks));

        initStrategy("sell-falling-ema90-1",
                timeFrameStorage,
                1,
                BigDecimal.valueOf(0.005),
                Order.OrderType.SELL,
                ticks -> tradeStrategyFactory.createShortSellEma90FallingTrendStrategy1(timeFrame, ticks));
        initStrategy("sell-falling-ema90-2",
                timeFrameStorage,
                1,
                BigDecimal.valueOf(0.005),
                Order.OrderType.SELL,
                ticks -> tradeStrategyFactory.createShortSellEma90FallingTrendStrategy2(timeFrame, ticks));
        initStrategy("sell-falling-ema90-3",
                timeFrameStorage,
                1,
                BigDecimal.valueOf(0.005),
                Order.OrderType.SELL,
                ticks -> tradeStrategyFactory.createShortSellEma90FallingTrendStrategy3(timeFrame, ticks));
        initStrategy("sell-falling-ema90-4",
                timeFrameStorage,
                1,
                BigDecimal.valueOf(0.005),
                Order.OrderType.SELL,
                ticks -> tradeStrategyFactory.createShortSellEma90FallingTrendStrategy4(timeFrame, ticks));

        /*initStrategy("modified1-short-buy",
                timeFrameStorage,
                minVolume,
                ticks -> tradeStrategyFactory.createModifiedShortBuyStrategy1(ticks));
        initStrategy("modified2-short-buy",
                timeFrameStorage,
                minVolume,
                ticks -> tradeStrategyFactory.createModifiedShortBuyStrategy2(ticks));
        initStrategy("real-short-buy",
                timeFrameStorage,
                realVolume,
                ticks -> tradeStrategyFactory.createShortBuyStrategy(ticks));*/

        initStrategy("rising-trend",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createRisingTrendStrategy(timeFrame, ticks));
        initStrategy("rising-trend-modified",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createModifiedRisingTrendStrategy(timeFrame, ticks));

        return Arrays.asList(timeFrameStorage);
    }

    public List<TimeFrameStorage> bigDataStrategies(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);

        BigDecimal minVolume = properties.getTradeConfig().getMinBtcTradeAmount();
        initStrategy("modified2-buy-rising-ema90",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createModifiedShortBuyEma90RisingTrendStrategy2(timeFrame, ticks));
        initStrategy("rising-trend",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createRisingTrendStrategy(timeFrame, ticks));
        initStrategy("falling-trend",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.SELL,
                ticks -> tradeStrategyFactory.createFallingTrendStrategy(timeFrame, ticks));
        initStrategy("tma-strategy-corrected",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createRisingTripleEmaStrategyCorrected(timeFrame, ticks));
        initStrategy("tma-falling-strategy",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.SELL,
                ticks -> tradeStrategyFactory.createFallingTripleEmaStrategy(timeFrame, ticks));
        initStrategy("tma-strategy-2",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                ticks -> tradeStrategyFactory.createRisingTripleEmaStrategy2(timeFrame, ticks));
        return Arrays.asList(timeFrameStorage);
    }

    private void initStrategy(String name,
                              TimeFrameStorage timeFrameStorage,
                              int tradingRecordsCount,
                              BigDecimal volume,
                              Order.OrderType direction,
                              Function<TimeSeries, Strategy> createStrategyFunction) {
        TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
        TimeSeries timeSeries = new TimeSeries(timeFrameStorage.getCandles());
        Strategy strategy = createStrategyFunction.apply(timeSeries);
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(name, strategy, timeFrame, direction, volume);
        IntStream.rangeClosed(1, tradingRecordsCount).forEach(index -> poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(index, name, direction)));
        timeFrameStorage.addStrategy(poloniexStrategy);
    }

}
