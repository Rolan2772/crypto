package com.crypto.trade.poloniex.conrollers;

import com.crypto.trade.poloniex.services.export.AnalyticsExportService;
import com.crypto.trade.poloniex.services.export.CandlesExportService;
import com.crypto.trade.poloniex.services.export.OrdersExportService;
import com.crypto.trade.poloniex.services.export.TradesExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/ticks")
    public void exportTicks() {
        tradesExportService.exportData();
    }

    @GetMapping("/candles")
    public void exportCandles() {
        candlesExportService.exportData();
    }

    @GetMapping("/analytics")
    public void exportAnalytics() {
        analyticsExportService.exportData();
    }

    @GetMapping("/orders")
    public void exportOrders() {
        ordersExportService.exportData();
    }

    @GetMapping("/all")
    public void all() {
        tradesExportService.exportData();
        candlesExportService.exportData();
        analyticsExportService.exportData();
        ordersExportService.exportData();
    }
}
