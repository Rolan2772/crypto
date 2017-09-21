package com.crypto.trade.poloniex.services.analytics.model;

import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TradingRecord;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(staticName = "of")
public class AnalyticsData {

    private Strategy strategy;
    private TradingRecord tradingRecord;
    private int historyIndex;
}
