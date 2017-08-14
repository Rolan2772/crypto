package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.services.utils.CalculationsUtils;
import eu.verdelhan.ta4j.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class HistoryAnalyticsService implements AnalyticsService {

    @Override
    public TradingAction analyzeTick(Strategy strategy, Tick lastTick, int index, int historyIndex, boolean analyzeHistory, TradingRecord tradingRecord, BigDecimal volume) {
        TradingAction action = TradingAction.NO_ACTION;
        boolean shouldAnalyze = analyzeHistory || index >= historyIndex;
        BigDecimal amount = CalculationsUtils.divide(volume, CalculationsUtils.toBigDecimal(lastTick.getClosePrice()));
        if (shouldAnalyze) {
            if (strategy.shouldEnter(index, tradingRecord)) {
                log.trace("Strategy should ENTER on {}", index);
                action = TradingAction.SHOULD_ENTER;
                boolean entered = tradingRecord.enter(index, lastTick.getClosePrice(), CalculationsUtils.toDecimal(amount));
                if (entered) {
                    Order entry = tradingRecord.getLastEntry();
                    action = TradingAction.ENTERED;
                    log.trace("Entered on {} (price={}, amount={})", entry.getIndex(), entry.getPrice().toDouble(), entry.getAmount().toDouble());
                }
            } else if (strategy.shouldExit(index, tradingRecord)) {
                log.trace("Strategy should EXIT on {}", index);
                action = TradingAction.SHOULD_EXIT;
                boolean exited = tradingRecord.exit(index, lastTick.getClosePrice(), CalculationsUtils.toDecimal(amount));
                if (exited) {
                    Order exit = tradingRecord.getLastExit();
                    action = TradingAction.EXITED;
                    log.trace("Exited on {} (price={}, amount={})", exit.getIndex(), exit.getPrice().toDouble(), exit.getAmount().toDouble());
                }
            }
        }
        return action;
    }
}