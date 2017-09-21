package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.services.analytics.model.AnalyticsData;
import com.crypto.trade.poloniex.services.analytics.model.TradeData;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TradingRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RealTimeAnalyticsService implements AnalyticsService {

    @Override
    public TradingAction analyzeTick(AnalyticsData analyticsData, TradeData tradeData) {
        Strategy strategy = analyticsData.getStrategy();
        TradingRecord tradingRecord = analyticsData.getTradingRecord();
        int index = tradeData.getIndex();
        TradingAction action = TradingAction.NO_ACTION;
        if (strategy.shouldOperate(index, tradingRecord)) {
            action = tradingRecord.getCurrentTrade().isNew() ? TradingAction.SHOULD_ENTER : TradingAction.SHOULD_EXIT;
            log.debug("Strategy {} on {} with price: {}", action, index, tradeData.getTick().getClosePrice());
        }
        return action;
    }
}
