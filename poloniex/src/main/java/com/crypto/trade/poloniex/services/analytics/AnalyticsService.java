package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.services.analytics.model.AnalyticsData;
import com.crypto.trade.poloniex.services.analytics.model.TradeData;

public interface AnalyticsService {

    TradingAction analyzeTick(AnalyticsData analyticsData, TradeData tradeData);
}