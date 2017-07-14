package com.crypto.trade.poloniex.services.analytics;

import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TradingRecord;

import java.util.List;

public interface AnalyticsService {

    void analyzeAll(List<Strategy> strategies);

    String analyzeTick(Strategy strategy, Tick newTick, int index, TradingRecord tradingRecord);
}