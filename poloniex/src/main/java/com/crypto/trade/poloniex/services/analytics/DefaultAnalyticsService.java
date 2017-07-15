package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.services.export.CsvFileWriter;
import com.crypto.trade.poloniex.storage.TickersStorage;
import eu.verdelhan.ta4j.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DefaultAnalyticsService implements AnalyticsService {

    @Autowired
    private TickersStorage tickersStorage;
    @Autowired
    private CsvFileWriter csvFileWriter;

    @Override
    public void analyzeAll(List<Strategy> strategies) {

    }

    @Override
    public TradingAction analyzeTick(Strategy strategy, Tick lastTick, int index, TradingRecord tradingRecord) {
        TradingAction action = TradingAction.NO_ACTION;
        if (strategy.shouldEnter(index, tradingRecord)) {
            log.debug("Strategy should ENTER on {}", index);
            action = TradingAction.SHOULD_ENTER;
            boolean entered = tradingRecord.enter(index, lastTick.getClosePrice(), Decimal.TEN);
            if (entered) {
                Order entry = tradingRecord.getLastEntry();
                action = TradingAction.ENTERED;
                log.debug("Entered on {} (price={}, amount={})", entry.getIndex(), entry.getPrice().toDouble(), entry.getAmount().toDouble());
            }
        } else if (strategy.shouldExit(index, tradingRecord)) {
            log.debug("Strategy should EXIT on {}", index);
            action = TradingAction.SHOULD_EXIT;
            boolean exited = tradingRecord.exit(index, lastTick.getClosePrice(), Decimal.TEN);
            if (exited) {
                Order exit = tradingRecord.getLastExit();
                action = TradingAction.EXITED;
                log.debug("Exited on {} (price={}, amount={})", exit.getIndex(), exit.getPrice().toDouble(), exit.getAmount().toDouble());
            }
        }
        return action;
    }
}
