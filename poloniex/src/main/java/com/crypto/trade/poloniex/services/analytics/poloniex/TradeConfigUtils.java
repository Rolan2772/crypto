package com.crypto.trade.poloniex.services.analytics.poloniex;

import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.storage.model.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.model.PoloniexTradingRecord;
import com.crypto.trade.poloniex.storage.model.TimeFrameStorage;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Strategy;

import java.math.BigDecimal;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class TradeConfigUtils {

    public void initStrategy(String name,
                             TimeFrameStorage timeFrameStorage,
                             int tradingRecordsCount,
                             BigDecimal volume,
                             Order.OrderType direction,
                             Supplier<Strategy> strategyFactory) {
        TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
        Strategy strategy = strategyFactory.get();
        PoloniexStrategy poloniexStrategy = new PoloniexStrategy(name, strategy, timeFrame, direction, volume);
        IntStream.rangeClosed(1, tradingRecordsCount)
                .forEach(index -> poloniexStrategy.addTradingRecord(new PoloniexTradingRecord(index, name, direction)));
        timeFrameStorage.addStrategy(poloniexStrategy);
    }

}
