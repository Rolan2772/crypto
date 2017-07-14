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
    public String analyzeTick(Strategy strategy, Tick newTick, int index, TradingRecord tradingRecord) {
        StringBuilder action = new StringBuilder(",");

        if (strategy.shouldEnter(index, tradingRecord)) {
            log.debug("Strategy should ENTER on {}", index);
            action.insert(0, "shouldEnter");
            boolean entered = tradingRecord.enter(index, newTick.getClosePrice(), Decimal.TEN);
            if (entered) {
                Order entry = tradingRecord.getLastEntry();
                log.debug("Entered on {} (price={}, amount={})", entry.getIndex(), entry.getPrice().toDouble(), entry.getAmount().toDouble());
                action.append("entered");
            }
        } else if (strategy.shouldExit(index, tradingRecord)) {
            log.debug("Strategy should EXIT on {}", index);
            action.insert(0, "should exit");
            boolean exited = tradingRecord.exit(index, newTick.getClosePrice(), Decimal.TEN);
            if (exited) {
                Order exit = tradingRecord.getLastExit();
                log.debug("Exited on {} (price={}, amount={})", exit.getIndex(), exit.getPrice().toDouble(), exit.getAmount().toDouble());
                action.append("exited");
            }
        }
        return action.toString();
    }
}
