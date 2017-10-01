package com.crypto.trade.poloniex.services.analytics.poloniex;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.analytics.TradingAction;
import com.crypto.trade.poloniex.services.analytics.model.ExportedPoloniexOrder;
import com.crypto.trade.poloniex.services.analytics.model.StrategyConfig;
import com.crypto.trade.poloniex.services.analytics.model.StrategyRules;
import com.crypto.trade.poloniex.services.analytics.strategies.ShortBuyStrategyFactory;
import com.crypto.trade.poloniex.services.analytics.strategies.TmaStrategyFactory;
import com.crypto.trade.poloniex.services.analytics.strategies.TrendStrategyFactory;
import com.crypto.trade.poloniex.storage.model.PoloniexOrder;
import com.crypto.trade.poloniex.storage.model.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.model.PoloniexTradingRecord;
import com.crypto.trade.poloniex.storage.model.TimeFrameStorage;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TradingRecord;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static eu.verdelhan.ta4j.Order.OrderType.BUY;
import static eu.verdelhan.ta4j.Order.OrderType.SELL;

public class ExperimentalTradeConfigFactory {

    public static final String TMA_FALLING_STRATEGY = "tma-falling-strategy";
    public static final String TMA_STRATEGY_2 = "tma-strategy-2";
    public static final String RISING_TREND = "rising-trend";
    public static final String FALLING_TREND = "falling-trend";
    public static final String SHORT_BUY_EMA540 = "short-buy-ema540";
    public static final String RISING_TREND_MODIFIED = "rising-trend-modified";

    @Autowired
    private ShortBuyStrategyFactory shortBuyFactory;
    @Autowired
    private TrendStrategyFactory trendFactory;
    @Autowired
    private TmaStrategyFactory tmaFactory;

    public PoloniexTradingRecord createTradingRecordWithOrders(int id, String shortBuyName, List<ExportedPoloniexOrder> exportedOrders) {
        PoloniexTradingRecord poloniexTradingRecord = new PoloniexTradingRecord(id, shortBuyName, BUY);
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
        TimeFrameStorage storage = new TimeFrameStorage(timeFrame);

        StrategyConfig cfg = StrategyConfig.of(timeFrame, BigDecimal.valueOf(0.1), SELL, 1);
        storage.addStrategy(new PoloniexStrategy(TMA_FALLING_STRATEGY, cfg, tmaFactory.createFallingTmaStrategy(currencyPair, timeFrame)));
        storage.addStrategy(new PoloniexStrategy(TMA_STRATEGY_2, cfg, tmaFactory.createRisingTmaStrategy2(currencyPair, timeFrame)));
        return Collections.singletonList(storage);
    }

    public List<TimeFrameStorage> experimentOneStrategy(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage storage = new TimeFrameStorage(timeFrame);

        StrategyConfig cfg = StrategyConfig.of(timeFrame, BigDecimal.valueOf(0.1), BUY, 1);
        storage.addStrategy(new PoloniexStrategy(RISING_TREND, cfg, trendFactory.createRisingTrendStrategy(currencyPair, timeFrame)));
        storage.addStrategy(new PoloniexStrategy(RISING_TREND_MODIFIED, cfg, trendFactory.createModifiedRisingTrendStrategy(currencyPair, timeFrame)));

        return Collections.singletonList(storage);
    }

    private void initShortBuyVariations(CurrencyPair currencyPair, TimeFrameStorage storage, BigDecimal volume) {
        TimeFrame timeFrame = storage.getTimeFrame();
        String shortBuy = "short-buy-ema90";

        StrategyConfig cfg = StrategyConfig.of(timeFrame, volume, BUY, 1);
        storage.addStrategy(new PoloniexStrategy(shortBuy + "-1", cfg, shortBuyFactory.createShortBuyEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.ONE))));
        storage.addStrategy(new PoloniexStrategy(shortBuy + "-2", cfg, shortBuyFactory.createShortBuyEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(5)))));
        storage.addStrategy(new PoloniexStrategy(shortBuy + "-3", cfg, shortBuyFactory.createShortBuyEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.TEN))));
        storage.addStrategy(new PoloniexStrategy(shortBuy + "-4", cfg, shortBuyFactory.createShortBuyEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(25)))));
        storage.addStrategy(new PoloniexStrategy(shortBuy + "-5", cfg, shortBuyFactory.createShortBuyEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(50)))));
        storage.addStrategy(new PoloniexStrategy(shortBuy + "-6", cfg, shortBuyFactory.createShortBuyEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.HUNDRED))));
        storage.addStrategy(new PoloniexStrategy(SHORT_BUY_EMA540, cfg, shortBuyFactory.createShortBuyEma540Strategy(currencyPair, timeFrame)));
    }

    private void initShortSellVariations(CurrencyPair currencyPair, TimeFrameStorage storage, BigDecimal volume) {
        TimeFrame timeFrame = storage.getTimeFrame();
        String shortSell = "short-sell-ema90";
        StrategyConfig cfg = StrategyConfig.of(timeFrame, volume, SELL, 1);

        storage.addStrategy(new PoloniexStrategy(shortSell + "-1", cfg, shortBuyFactory.createShortSellEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.ONE))));
        storage.addStrategy(new PoloniexStrategy(shortSell + "-2", cfg, shortBuyFactory.createShortSellEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(5)))));
        storage.addStrategy(new PoloniexStrategy(shortSell + "-3", cfg, shortBuyFactory.createShortSellEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.TEN))));
        storage.addStrategy(new PoloniexStrategy(shortSell + "-4", cfg, shortBuyFactory.createShortSellEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(25)))));
        storage.addStrategy(new PoloniexStrategy(shortSell + "-5", cfg, shortBuyFactory.createShortSellEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(50)))));
        storage.addStrategy(new PoloniexStrategy(shortSell + "-6", cfg, shortBuyFactory.createShortSellEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.HUNDRED))));
    }

    public List<TimeFrameStorage> allTimeFramesAndStrategies(CurrencyPair currencyPair) {
        BigDecimal volume = BigDecimal.valueOf(0.08);
        return Arrays.stream(TimeFrame.values())
                .map(timeFrame -> {
                    TimeFrameStorage storage = new TimeFrameStorage(timeFrame);
                    initShortBuyVariations(currencyPair, storage, volume);
                    initShortSellVariations(currencyPair, storage, volume);
                    initTrendVariations(currencyPair, storage, volume);
                    initTmaVariations(currencyPair, storage, volume);
                    return storage;
                })
                .collect(Collectors.toList());
    }

    private void initTrendVariations(CurrencyPair currencyPair, TimeFrameStorage storage, BigDecimal volume) {
        TimeFrame timeFrame = storage.getTimeFrame();
        StrategyConfig buyCfg = StrategyConfig.of(timeFrame, volume, BUY, 1);
        storage.addStrategy(new PoloniexStrategy(RISING_TREND, buyCfg, trendFactory.createRisingTrendStrategy(currencyPair, timeFrame)));
        storage.addStrategy(new PoloniexStrategy(RISING_TREND_MODIFIED, buyCfg, trendFactory.createModifiedRisingTrendStrategy(currencyPair, timeFrame)));
        StrategyConfig sellCfg = StrategyConfig.of(timeFrame, volume, SELL, 1);
        storage.addStrategy(new PoloniexStrategy(FALLING_TREND, sellCfg, trendFactory.createFallingTrendStrategy(currencyPair, timeFrame)));
    }

    private void initTmaVariations(CurrencyPair currencyPair, TimeFrameStorage storage, BigDecimal volume) {
        TimeFrame timeFrame = storage.getTimeFrame();
        StrategyConfig buyCfg = StrategyConfig.of(timeFrame, volume, BUY, 1);
        storage.addStrategy(new PoloniexStrategy("tma-strategy-corrected", buyCfg, tmaFactory.createRisingTripleEmaStrategyCorrected(currencyPair, timeFrame)));
        storage.addStrategy(new PoloniexStrategy("tma-strategy-2", buyCfg, tmaFactory.createRisingTmaStrategy2(currencyPair, timeFrame)));
        StrategyConfig sellCfg = StrategyConfig.of(timeFrame, volume, SELL, 1);
        storage.addStrategy(new PoloniexStrategy(TMA_FALLING_STRATEGY, sellCfg, tmaFactory.createFallingTmaStrategy(currencyPair, timeFrame)));
    }

    public List<TimeFrameStorage> bigDataStrategies(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        TimeFrameStorage storage = new TimeFrameStorage(timeFrame);
        BigDecimal volume = BigDecimal.valueOf(0.1);

        StrategyConfig buyCfg = StrategyConfig.of(timeFrame, volume, BUY, 1);
        storage.addStrategy(new PoloniexStrategy("top-short-buy-ema90", buyCfg, shortBuyFactory.createShortBuyEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(25)))));
        storage.addStrategy(new PoloniexStrategy(SHORT_BUY_EMA540, buyCfg, shortBuyFactory.createShortBuyEma540Strategy(currencyPair, timeFrame)));
        storage.addStrategy(new PoloniexStrategy(RISING_TREND, buyCfg, trendFactory.createRisingTrendStrategy(currencyPair, timeFrame)));

        StrategyConfig sellCfg = StrategyConfig.of(timeFrame, volume, SELL, 1);
        storage.addStrategy(new PoloniexStrategy(FALLING_TREND, sellCfg, trendFactory.createFallingTrendStrategy(currencyPair, timeFrame)));
        initTmaVariations(currencyPair, storage, volume);

        return Collections.singletonList(storage);
    }
}

