package com.crypto.trade.poloniex.services.analytics.poloniex;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.analytics.strategies.ShortBuyStrategyFactory;
import com.crypto.trade.poloniex.services.analytics.strategies.TrendStrategyFactory;
import com.crypto.trade.poloniex.services.analytics.strategies.TripleEmaStrategyFactory;
import com.crypto.trade.poloniex.storage.model.TimeFrameStorage;
import eu.verdelhan.ta4j.Order;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RealTradeConfFactory {

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

    public List<TimeFrameStorage> createRealAmountBuyConf1(CurrencyPair currencyPair) {
        String strategyName = "real-shot-buy-conf-1";
        BigDecimal volume = properties.getTradeConfig().getRealBtcTradeAmount();
        TimeFrame timeFrame1 = TimeFrame.ONE_MINUTE;
        TimeFrameStorage timeFrameStorage1 = new TimeFrameStorage(timeFrame1);
        tradeConfigUtils.initStrategy(strategyName,
                timeFrameStorage1,
                10,
                volume,
                Order.OrderType.BUY,
                () -> shortBuyStrategyFactory.createShortBuyStrategy(currencyPair, timeFrame1));

        TimeFrame timeFrame5 = TimeFrame.FIVE_MINUTES;
        TimeFrameStorage timeFrameStorage5 = new TimeFrameStorage(timeFrame5);
        tradeConfigUtils.initStrategy(strategyName,
                timeFrameStorage5,
                3,
                volume,
                Order.OrderType.BUY,
                () -> shortBuyStrategyFactory.createShortBuyStrategy(currencyPair, timeFrame5));
        return Arrays.asList(timeFrameStorage1, timeFrameStorage5);
    }

    public List<TimeFrameStorage> createRealAmountBuyConf2(CurrencyPair currencyPair) {
        TimeFrame timeFrame = TimeFrame.ONE_MINUTE;
        BigDecimal volume = BigDecimal.valueOf(0.08);

        TimeFrameStorage timeFrameStorage = new TimeFrameStorage(timeFrame);
        tradeConfigUtils.initStrategy("modified2-buy-rising-ema90",
                timeFrameStorage,
                1,
                volume,
                Order.OrderType.BUY,
                () -> shortBuyStrategyFactory.createShortBuyEma90Strategy2(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("rising-trend",
                timeFrameStorage,
                1,
                volume,
                Order.OrderType.BUY,
                () -> trendStrategyFactory.createRisingTrendStrategy(currencyPair, timeFrame));
        tradeConfigUtils.initStrategy("tma-strategy-2",
                timeFrameStorage,
                1,
                volume,
                Order.OrderType.BUY,
                () -> tripleEmaStrategyFactory.createRisingTripleEmaStrategy2(currencyPair, timeFrame));
        return Collections.singletonList(timeFrameStorage);
    }

}
