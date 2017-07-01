package com.crypto.trade.bittrex.storage;

import com.crypto.trade.bittrex.dto.BittrexCurrency;
import com.crypto.trade.bittrex.dto.BittrexMarketItem;
import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Data
public class DataStorage {

    private ConcurrentMap<String, BittrexCurrency> currencies = new ConcurrentHashMap<>();
    private ConcurrentMap<String, BittrexMarketItem> markets = new ConcurrentHashMap<>();

    public void addCcy(BittrexCurrency ccy) {
        currencies.put(ccy.getCurrency(), ccy);
    }

    public void addMarket(BittrexMarketItem market) {
        markets.put(market.getMarketName(), market);
    }
}
