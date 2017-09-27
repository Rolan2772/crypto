package com.crypto.trade.poloniex.services.analytics.poloniex;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.analytics.TradingAction;
import com.crypto.trade.poloniex.services.analytics.model.ExportedPoloniexOrder;
import com.crypto.trade.poloniex.services.analytics.strategies.ShortBuyStrategyFactory;
import com.crypto.trade.poloniex.services.analytics.strategies.TrendStrategyFactory;
import com.crypto.trade.poloniex.services.analytics.strategies.TripleEmaStrategyFactory;
import com.crypto.trade.poloniex.storage.model.PoloniexOrder;
import com.crypto.trade.poloniex.storage.model.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.model.PoloniexTradingRecord;
import com.crypto.trade.poloniex.storage.model.TimeFrameStorage;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TradingRecord;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExperimentalTradeConfigFactory {

    @Autowired
    private PoloniexProperties properties;
    @Autowired
    private ShortBuyStrategyFactory shortBuyStrategyFactory;
    @Autowired
    private TrendStrategyFactory trendStrategyFactory;
    @Autowired
    private TripleEmaStrategyFactory tripleEmaStrategyFactory;
    @Autowired
    private TradeConfigUtils tradeConfigUtils;

    public List<TimeFrameStorage> createTestProfitCalculationsStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        String shortBuyName = "profit-test-short-buy-1";
        Strategy shortBuyStrategy = shortBuyStrategyFactory.createShortBuyStrategy(currencyPair, timeFrame);
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
        Strategy shortBuyStrategy1 = shortBuyStrategyFactory.createShortBuyStrategy(currencyPair, timeFrame);
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
        Strategy shortBuyStrategy = shortBuyStrategyFactory.createShortBuyStrategy(currencyPair, timeFrame);
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


    public List<TimeFrameStorage> createTopPerformingStrategies(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        tradeConfigUtils.initStrategy("tma-falling-strategy",
                timeFrameStorage,
                1,
                BigDecimal.valueOf(0.1),
                Order.OrderType.SELL,
                () -> tripleEmaStrategyFactory.createFallingTripleEmaStrategy(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("tma-strategy-2",
                timeFrameStorage,
                1,
                BigDecimal.valueOf(0.01),
                Order.OrderType.BUY,
                () -> tripleEmaStrategyFactory.createRisingTripleEmaStrategy2(currencyPair, timeFrame));
        return Collections.singletonList(timeFrameStorage);
    }

    public List<TimeFrameStorage> experimentOneStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);

        BigDecimal minVolume = properties.getTradeConfig().getMinBtcTradeAmount();

        tradeConfigUtils.initStrategy("short-buy-rising-ema90",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                () -> shortBuyStrategyFactory.createShortBuyEma90Strategy(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("short-buy-rising-ema90-1",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                () -> shortBuyStrategyFactory.createShortBuyEma90Strategy1(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("short-buy-rising-ema90-2",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                () -> shortBuyStrategyFactory.createShortBuyEma90Strategy2(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("short-buy-rising-ema90-3",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                () -> shortBuyStrategyFactory.createShortBuyEma90Strategy3(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("short-buy-rising-ema90-4",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                () -> shortBuyStrategyFactory.createShortBuyEma90Strategy4(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("short-buy-rising-ema90-5",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                () -> shortBuyStrategyFactory.createShortBuyEma90Strategy5(currencyPair, timeFrame));

        tradeConfigUtils.initStrategy("short-sell-falling-ema90",
                timeFrameStorage,
                1,
                BigDecimal.valueOf(0.005),
                Order.OrderType.SELL,
                () -> shortBuyStrategyFactory.createShortSellEma90Strategy(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("short-sell-falling-ema90-1",
                timeFrameStorage,
                1,
                BigDecimal.valueOf(0.005),
                Order.OrderType.SELL,
                () -> shortBuyStrategyFactory.createShortSellEma90Strategy1(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("short-sell-falling-ema90-2",
                timeFrameStorage,
                1,
                BigDecimal.valueOf(0.005),
                Order.OrderType.SELL,
                () -> shortBuyStrategyFactory.createShortSellEma90Strategy2(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("short-sell-falling-ema90-3",
                timeFrameStorage,
                1,
                BigDecimal.valueOf(0.005),
                Order.OrderType.SELL,
                () -> shortBuyStrategyFactory.createShortSellEma90Strategy3(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("short-sell-falling-ema90-4",
                timeFrameStorage,
                1,
                BigDecimal.valueOf(0.005),
                Order.OrderType.SELL,
                () -> shortBuyStrategyFactory.createShortSellEma90Strategy4(currencyPair, timeFrame));

        tradeConfigUtils.initStrategy("short-sell-falling-ema90-5",
                timeFrameStorage,
                1,
                BigDecimal.valueOf(0.005),
                Order.OrderType.SELL,
                () -> shortBuyStrategyFactory.createShortSellEma90Strategy5(currencyPair, timeFrame));

        tradeConfigUtils.initStrategy("rising-trend",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                () -> trendStrategyFactory.createRisingTrendStrategy(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("rising-trend-modified",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                () -> trendStrategyFactory.createModifiedRisingTrendStrategy(currencyPair, timeFrame));

        return Collections.singletonList(timeFrameStorage);
    }

    public List<TimeFrameStorage> allTimeFramesAndStrategies(CurrencyPair currencyPair) {
        return Arrays.stream(TimeFrame.values())
                .map(timeFrame -> {
                    TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);

                    BigDecimal volume = BigDecimal.valueOf(0.08);
                    tradeConfigUtils.initStrategy("short-buy-rising-ema90",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.BUY,
                            () -> shortBuyStrategyFactory.createShortBuyEma90Strategy(currencyPair, timeFrame));
                    tradeConfigUtils.initStrategy("short-buy-rising-ema90-1",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.BUY,
                            () -> shortBuyStrategyFactory.createShortBuyEma90Strategy1(currencyPair, timeFrame));
                    tradeConfigUtils.initStrategy("short-buy-rising-ema90-2",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.BUY,
                            () -> shortBuyStrategyFactory.createShortBuyEma90Strategy2(currencyPair, timeFrame));

                    tradeConfigUtils.initStrategy("short-buy-rising-ema90-3",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.BUY,
                            () -> shortBuyStrategyFactory.createShortBuyEma90Strategy3(currencyPair, timeFrame));
                    tradeConfigUtils.initStrategy("short-buy-rising-ema90-4",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.BUY,
                            () -> shortBuyStrategyFactory.createShortBuyEma90Strategy4(currencyPair, timeFrame));
                    tradeConfigUtils.initStrategy("short-buy-rising-ema90-5",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.BUY,
                            () -> shortBuyStrategyFactory.createShortBuyEma90Strategy4(currencyPair, timeFrame));

                    tradeConfigUtils.initStrategy("short-buy-rising-ema540",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.BUY,
                            () -> shortBuyStrategyFactory.createShortBuyEma540Strategy(currencyPair, timeFrame));

                    tradeConfigUtils.initStrategy("short-sell-falling-ema90",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.SELL,
                            () -> shortBuyStrategyFactory.createShortSellEma90Strategy1(currencyPair, timeFrame));
                    tradeConfigUtils.initStrategy("short-sell-falling-ema90-1",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.SELL,
                            () -> shortBuyStrategyFactory.createShortSellEma90Strategy2(currencyPair, timeFrame));
                    tradeConfigUtils.initStrategy("short-sell-falling-ema90-2",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.SELL,
                            () -> shortBuyStrategyFactory.createShortSellEma90Strategy3(currencyPair, timeFrame));
                    tradeConfigUtils.initStrategy("sell-falling-ema90-4",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.SELL,
                            () -> shortBuyStrategyFactory.createShortSellEma90Strategy4(currencyPair, timeFrame));
                    tradeConfigUtils.initStrategy("sell-falling-ema90-4",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.SELL,
                            () -> shortBuyStrategyFactory.createShortSellEma90Strategy5(currencyPair, timeFrame));

                    tradeConfigUtils.initStrategy("rising-trend",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.BUY,
                            () -> trendStrategyFactory.createRisingTrendStrategy(currencyPair, timeFrame));
                    tradeConfigUtils.initStrategy("rising-trend-modified",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.BUY,
                            () -> trendStrategyFactory.createModifiedRisingTrendStrategy(currencyPair, timeFrame));
                    tradeConfigUtils.initStrategy("falling-trend",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.SELL,
                            () -> trendStrategyFactory.createFallingTrendStrategy(currencyPair, timeFrame));

                    tradeConfigUtils.initStrategy("tma-strategy-corrected",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.BUY,
                            () -> tripleEmaStrategyFactory.createRisingTripleEmaStrategyCorrected(currencyPair, timeFrame));
                    tradeConfigUtils.initStrategy("tma-falling-strategy",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.SELL,
                            () -> tripleEmaStrategyFactory.createFallingTripleEmaStrategy(currencyPair, timeFrame));
                    tradeConfigUtils.initStrategy("tma-strategy-2",
                            timeFrameStorage,
                            1,
                            volume,
                            Order.OrderType.BUY,
                            () -> tripleEmaStrategyFactory.createRisingTripleEmaStrategy2(currencyPair, timeFrame));
                    return timeFrameStorage;
                })
                .collect(Collectors.toList());
    }

    public List<TimeFrameStorage> bigDataStrategies(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);

        BigDecimal minVolume = properties.getTradeConfig().getMinBtcTradeAmount();
        tradeConfigUtils.initStrategy("short-buy-rising-ema90-2",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                () -> shortBuyStrategyFactory.createShortBuyEma90Strategy2(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("short-buy-rising-ema540",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                () -> shortBuyStrategyFactory.createShortBuyEma540Strategy(currencyPair, timeFrame));
        
        tradeConfigUtils.initStrategy("rising-trend",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                () -> trendStrategyFactory.createRisingTrendStrategy(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("falling-trend",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.SELL,
                () -> trendStrategyFactory.createFallingTrendStrategy(currencyPair, timeFrame));

        tradeConfigUtils.initStrategy("tma-strategy-corrected",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                () -> tripleEmaStrategyFactory.createRisingTripleEmaStrategyCorrected(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("tma-falling-strategy",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.SELL,
                () -> tripleEmaStrategyFactory.createFallingTripleEmaStrategy(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("tma-strategy-2",
                timeFrameStorage,
                1,
                minVolume,
                Order.OrderType.BUY,
                () -> tripleEmaStrategyFactory.createRisingTripleEmaStrategy2(currencyPair, timeFrame));
        return Collections.singletonList(timeFrameStorage);
    }
}
