package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;

import java.util.Collection;

public interface ExportDataService<T> {

    void exportData(CurrencyPair currencyPair, Collection<T> data);

    // @TODO: move to another interface
    void exportData(String name, Collection<T> data, boolean append);
}
