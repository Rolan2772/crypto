package com.crypto.trade.poloniex.services.analytics.poloniex;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.analytics.model.StrategyConfig;
import com.crypto.trade.poloniex.services.analytics.model.StrategyRules;
import com.crypto.trade.poloniex.services.analytics.strategies.ShortBuyStrategyFactory;
import com.crypto.trade.poloniex.services.analytics.strategies.TmaStrategyFactory;
import com.crypto.trade.poloniex.services.analytics.strategies.TrendStrategyFactory;
import com.crypto.trade.poloniex.storage.model.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.model.TimeFrameStorage;
import eu.verdelhan.ta4j.Decimal;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static eu.verdelhan.ta4j.Order.OrderType.BUY;

public class RealTradeConfFactory {

    @Autowired
    private PoloniexProperties properties;
    @Autowired
    private ShortBuyStrategyFactory shortBuyFactory;
    @Autowired
    private TrendStrategyFactory trendStrategyFactory;
    @Autowired
    private TmaStrategyFactory tmaStrategyFactory;

    public List<TimeFrameStorage> createRealAmountBuyConf1(CurrencyPair currencyPair) {
        String name = "real-shot-buy-conf";

        TimeFrame oneMinute = TimeFrame.ONE_MINUTE;
        BigDecimal volume = properties.getTradeConfig().getRealBtcTradeAmount();
        StrategyConfig oneMinuteCfg = StrategyConfig.of(oneMinute, volume, BUY, 10);
        TimeFrameStorage oneMinuteStorage = new TimeFrameStorage(oneMinute);
        oneMinuteStorage.addStrategy(new PoloniexStrategy(name, oneMinuteCfg, shortBuyFactory.createShortBuyStrategy(currencyPair, oneMinute)));

        TimeFrame fiveMinutes = TimeFrame.FIVE_MINUTES;
        TimeFrameStorage fiveMinuteStorage = new TimeFrameStorage(fiveMinutes);
        StrategyConfig fiveMinutesCfg = StrategyConfig.of(fiveMinutes, volume, BUY, 3);
        oneMinuteStorage.addStrategy(new PoloniexStrategy(name, fiveMinutesCfg, shortBuyFactory.createShortBuyStrategy(currencyPair, fiveMinutes)));
        return Arrays.asList(oneMinuteStorage, fiveMinuteStorage);
    }

    public List<TimeFrameStorage> createRealAmountBuyConf2(CurrencyPair currencyPair) {
        TimeFrame oneMinute = TimeFrame.ONE_MINUTE;
        TimeFrameStorage storage = new TimeFrameStorage(oneMinute);
        BigDecimal volume = BigDecimal.valueOf(0.08);

        StrategyConfig cfg = StrategyConfig.of(oneMinute, volume, BUY, 1);
        storage.addStrategy(new PoloniexStrategy("top-short-buy-ema90", cfg, shortBuyFactory.createShortBuyEma90(currencyPair, oneMinute, StrategyRules.of(Decimal.ONE, Decimal.valueOf(25)))));
        storage.addStrategy(new PoloniexStrategy("top-rising-trend", cfg, trendStrategyFactory.createRisingTrendStrategy(currencyPair, oneMinute)));
        storage.addStrategy(new PoloniexStrategy("top-tma-rising", cfg, tmaStrategyFactory.createRisingTmaStrategy2(currencyPair, oneMinute)));
        return Collections.singletonList(storage);
    }
}
