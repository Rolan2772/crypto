package com.crypto.trade.poloniex.services.analytics;

import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TradingRecord;

import java.math.BigDecimal;

public interface AnalyticsService {

    TradingAction analyzeTick(Strategy strategy, Tick lastTick, int index, int historyIndex, boolean analyzeHistory, TradingRecord tradingRecord, BigDecimal volume);
}