package com.crypto.trade.poloniex.storage;

import com.crypto.trade.poloniex.dto.PoloniexHistoryTrade;
import com.crypto.trade.poloniex.dto.PoloniexTrade;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
public class TradesStorage {

    @Autowired
    private CandlesStorage candlesStorage;

    @Getter
    private ConcurrentMap<CurrencyPair, List<PoloniexTrade>> trades = new ConcurrentHashMap<>();

    public void addTrade(CurrencyPair currency, PoloniexTrade poloniexTrade) {
        initCurrency(currency);
        trades.get(currency).add(poloniexTrade);
        candlesStorage.addTrade(currency, poloniexTrade);
    }

    public void initCurrency(CurrencyPair currency) {
        trades.computeIfAbsent(currency, s -> new CopyOnWriteArrayList<>());
    }

    public void addTradesHistory(CurrencyPair currency, List<PoloniexHistoryTrade> items) {
        List<PoloniexTrade> currencyTrades = trades.get(currency);
        currencyTrades.addAll(items.stream()
                .map(PoloniexTrade::new)
                .collect(Collectors.toList()));
        currencyTrades.sort((o1, o2) -> {
            int result = o1.getTradeTime().compareTo(o2.getTradeTime());
            if (result == 0) {
                result = o1.getTradeId().compareTo(o2.getTradeId());
            }
            return result;
        });
        candlesStorage.addTradesHistory(currency, currencyTrades);
    }

    public String getLastTrade(CurrencyPair currencyPair) {
        List<PoloniexTrade> pairTrades = trades.getOrDefault(currencyPair, Collections.emptyList());
        return pairTrades.get(pairTrades.size() - 1).getRate();
    }
}