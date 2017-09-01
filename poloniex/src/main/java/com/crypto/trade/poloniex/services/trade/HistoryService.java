package com.crypto.trade.poloniex.services.trade;

import com.crypto.trade.poloniex.dto.PoloniexHistoryTrade;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public interface HistoryService {

    List<PoloniexHistoryTrade> loadTradesHistory(CurrencyPair currencyPair, Duration historyDuration);

    List<PoloniexHistoryTrade> loadTradesHistory(CurrencyPair currencyPair, LocalDateTime start, LocalDateTime end);
}
