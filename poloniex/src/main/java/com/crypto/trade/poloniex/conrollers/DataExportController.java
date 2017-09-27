package com.crypto.trade.poloniex.conrollers;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.export.*;
import com.crypto.trade.poloniex.storage.CandlesStorage;
import com.crypto.trade.poloniex.storage.model.TimeFrameStorage;
import com.crypto.trade.poloniex.storage.TradesStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

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

    @GetMapping("/{currency}/orders")
    public void orders(@PathVariable CurrencyPair currency) {
        List<TimeFrameStorage> candlesData = candlesStorage.getData(currency);
        ordersExportService.exportMemoryData(currency, candlesData);
    }

    @GetMapping("/{currency}/orders/{type}")
    public void osOrders(@PathVariable CurrencyPair currency, @PathVariable OsType type) {
        CurrencyPair currencyPair = CurrencyPair.BTC_ETH;
        List<TimeFrameStorage> candlesData = candlesStorage.getData(currencyPair);
        ordersExportService.exportMemoryData(currencyPair, candlesData, type);
    }

    @GetMapping("/all")
    public void all() {
        Arrays.stream(CurrencyPair.values())
                .forEach(this::export);
    }

    @GetMapping("/{currency}/all")
    public void all(@PathVariable CurrencyPair currency) {
        export(currency);
    }

    private void export(CurrencyPair currency) {
        //        SortedSet<PoloniexTrade> trades = tradesStorage.getTrades(currency);
//        tradesExportService.exportMemoryData(currency, trades);
        List<TimeFrameStorage> candlesData = candlesStorage.getData(currency);
//        candlesExportService.exportMemoryData(currencyPair, candlesData);
        analyticsExportService.exportMemoryData(currency, candlesData);
        ordersExportService.exportMemoryData(currency, candlesData);
    }

    @GetMapping("/{currency}/{type}")
    public void allWithType(@PathVariable CurrencyPair currency, @PathVariable OsType type) {
        export(currency, type);

    }

    private void export(CurrencyPair currency, OsType type) {
//      SortedSet<PoloniexTrade> trades = tradesStorage.getTrades(currency);
//      tradesExportService.exportMemoryData(currency, trades, type);
        List<TimeFrameStorage> candlesData = candlesStorage.getData(currency);
//        candlesExportService.exportMemoryData(currencyPair, candlesData, type);
        analyticsExportService.exportMemoryData(currency, candlesData, type);
        ordersExportService.exportMemoryData(currency, candlesData, type);
    }
}
