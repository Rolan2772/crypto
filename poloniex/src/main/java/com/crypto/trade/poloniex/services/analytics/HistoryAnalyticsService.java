package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.services.analytics.model.AnalyticsData;
import com.crypto.trade.poloniex.services.analytics.model.TradeData;
import com.crypto.trade.poloniex.services.trade.TradeCalculator;
import com.crypto.trade.poloniex.services.utils.CalculationsUtils;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TradingRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class HistoryAnalyticsService implements AnalyticsService {

    @Override
    public TradingAction analyzeTick(AnalyticsData analyticsData, TradeData tradeData) {
        Strategy strategy = analyticsData.getStrategy();
        TradingRecord tradingRecord = analyticsData.getTradingRecord();
        int index = tradeData.getIndex();
        Decimal price = tradeData.getTick().getClosePrice();
        TradingAction action = TradingAction.NO_ACTION;
        BigDecimal closePrice = CalculationsUtils.toBigDecimal(price);
        boolean shouldAnalyze = index >= analyticsData.getHistoryIndex();
        BigDecimal entryAmount = TradeCalculator.getEntryAmount(tradeData.getVolume(), closePrice, tradeData.getDirection());
        if (shouldAnalyze) {
            if (strategy.shouldEnter(index, tradingRecord)) {
                log.trace("Strategy should ENTER on {}", index);
                action = TradingAction.SHOULD_ENTER;
                boolean entered = tradingRecord.enter(index, price, CalculationsUtils.toDecimal(entryAmount));
                if (entered) {
                    Order entry = tradingRecord.getLastEntry();
                    action = TradingAction.ENTERED;
                    log.trace("Entered on {} (price={}, amount={})", entry.getIndex(), entry.getPrice().toDouble(), entry.getAmount().toDouble());
                }
            } else if (strategy.shouldExit(index, tradingRecord)) {
                log.trace("Strategy should EXIT on {}", index);
                action = TradingAction.SHOULD_EXIT;
                Order entry = tradingRecord.getCurrentTrade().getEntry();
                BigDecimal exitAmount = entry == null
                        ? entryAmount
                        : TradeCalculator.getExitAmount(entry, closePrice);
                boolean exited = tradingRecord.exit(index, price, CalculationsUtils.toDecimal(exitAmount));
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