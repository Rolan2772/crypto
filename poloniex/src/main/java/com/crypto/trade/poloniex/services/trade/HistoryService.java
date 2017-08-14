package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.dto.PoloniexHistoryTrade;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;

import java.time.Duration;
import java.util.List;

public interface HistoryService {

    List<PoloniexHistoryTrade> loadTradesHistory(CurrencyPair currencyPair, Duration historyDuration);
}
