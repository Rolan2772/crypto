package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.services.trade.TradeCalculator;
import com.crypto.trade.poloniex.services.utils.CalculationsUtils;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TradingRecord;
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
        BigDecimal amount = TradeCalculator.getAmount(volume, CalculationsUtils.toBigDecimal(lastTick.getClosePrice()));
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
                Order entry = tradingRecord.getCurrentTrade().getEntry();
                BigDecimal amountToSell = entry == null ? amount : TradeCalculator.getAmountWithFee(tradingRecord.getCurrentTrade().getEntry());
                boolean exited = tradingRecord.exit(index, lastTick.getClosePrice(), CalculationsUtils.toDecimal(amountToSell));
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