package com.crypto.trade.poloniex.services.analytics;

import eu.verdelhan.ta4j.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HistoryAnalyticsService implements AnalyticsService {

    @Override
    public TradingAction analyzeTick(Strategy strategy, Tick lastTick, int index, TradingRecord tradingRecord) {
        TradingAction action = TradingAction.NO_ACTION;
        if (strategy.shouldEnter(index, tradingRecord)) {
            log.trace("Strategy should ENTER on {}", index);
            action = TradingAction.SHOULD_ENTER;
            boolean entered = tradingRecord.enter(index, lastTick.getClosePrice(), Decimal.ONE);
            if (entered) {
                Order entry = tradingRecord.getLastEntry();
                action = TradingAction.ENTERED;
                log.trace("Entered on {} (price={}, amount={})", entry.getIndex(), entry.getPrice().toDouble(), entry.getAmount().toDouble());
            }
        } else if (strategy.shouldExit(index, tradingRecord)) {
            log.trace("Strategy should EXIT on {}", index);
            action = TradingAction.SHOULD_EXIT;
            boolean exited = tradingRecord.exit(index, lastTick.getClosePrice(), Decimal.ONE);
            if (exited) {
                Order exit = tradingRecord.getLastExit();
                action = TradingAction.EXITED;
                log.trace("Exited on {} (price={}, amount={})", exit.getIndex(), exit.getPrice().toDouble(), exit.getAmount().toDouble());
            }
        }
        return action;
    }
}