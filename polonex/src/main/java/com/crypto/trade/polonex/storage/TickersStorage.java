package com.crypto.trade.polonex.storage;

import com.crypto.trade.polonex.dto.Ticker;
import lombok.Getter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class TickersStorage {

    @Getter
    private ConcurrentMap<String, Set<Ticker>> tickers = new ConcurrentHashMap<>();

    public void addTicker(Ticker ticker) {
        String key = ticker.getCurrencyPair();
        Set<Ticker> ccyTickers = tickers.getOrDefault(ticker.getCurrencyPair(), new ConcurrentSkipListSet<>());
        ccyTickers.add(ticker);
        tickers.put(key, ccyTickers);
    }
}
