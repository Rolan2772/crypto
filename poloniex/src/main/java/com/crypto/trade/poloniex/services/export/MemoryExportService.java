package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;

import java.util.Collection;

public interface MemoryExportService<T> {

    void exportMemoryData(CurrencyPair currencyPair, Collection<T> data);

    void exportMemoryData(CurrencyPair currencyPair, Collection<T> data, OsType osType);
}
