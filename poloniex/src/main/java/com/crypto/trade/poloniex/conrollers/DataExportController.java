package com.crypto.trade.poloniex.conrollers;

import com.crypto.trade.poloniex.dto.PoloniexTrade;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.export.*;
import com.crypto.trade.poloniex.storage.CandlesStorage;
import com.crypto.trade.poloniex.storage.TimeFrameStorage;
import com.crypto.trade.poloniex.storage.TradesStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.SortedSet;

@RestController
@RequestMapping("/export")
public class DataExportController {

    @Autowired
    private TradesExportService tradesExportService;
    @Autowired
    private CandlesExportService candlesExportService;
    @Autowired
    private AnalyticsExportService analyticsExportService;
    @Autowired
    private OrdersExportService ordersExportService;
    @Autowired
    private CandlesStorage candlesStorage;
    @Autowired
    private TradesStorage tradesStorage;

    @GetMapping("/all")
    public void all() {
        CurrencyPair currencyPair = CurrencyPair.BTC_ETH;
        SortedSet<PoloniexTrade> trades = tradesStorage.getTrades(currencyPair);
        tradesExportService.exportMemoryData(currencyPair, trades);
        List<TimeFrameStorage> candlesData = candlesStorage.getData(currencyPair);
        //candlesExportService.exportMemoryData(currencyPair, candlesData);
        analyticsExportService.exportMemoryData(currencyPair, candlesData);
        ordersExportService.exportMemoryData(currencyPair, candlesData);
    }

    @GetMapping("/{type}")
    public void allWithType(@PathVariable OsType type) {
        CurrencyPair currencyPair = CurrencyPair.BTC_ETH;
        SortedSet<PoloniexTrade> trades = tradesStorage.getTrades(currencyPair);
        tradesExportService.exportMemoryData(currencyPair, trades, type);
        List<TimeFrameStorage> candlesData = candlesStorage.getData(currencyPair);
        //candlesExportService.exportMemoryData(currencyPair, candlesData, type);
        analyticsExportService.exportMemoryData(currencyPair, candlesData, type);
        ordersExportService.exportMemoryData(currencyPair, candlesData, type);
    }
}
