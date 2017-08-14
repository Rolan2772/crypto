package com.crypto.trade.poloniex.services.analytics;

import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TradingRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class RealTimeAnalyticsService implements AnalyticsService {

    @Override
    public TradingAction analyzeTick(Strategy strategy, Tick lastTick, int index, int historyIndex, boolean analyzeHistory, TradingRecord tradingRecord, BigDecimal volume) {
        TradingAction action = TradingAction.NO_ACTION;
        if (tradingRecord.getCurrentTrade().isNew() && strategy.shouldEnter(index, tradingRecord)) {
            log.debug("Strategy should ENTER on {} with price: {}", index, lastTick.getClosePrice());
            action = TradingAction.SHOULD_ENTER;
        } else if (tradingRecord.getCurrentTrade().isOpened() && strategy.shouldExit(index, tradingRecord)) {
            log.debug("Strategy should EXIT on {} with price: {}", index, lastTick.getClosePrice());
            action = TradingAction.SHOULD_EXIT;
        }
        return action;
    }
}
