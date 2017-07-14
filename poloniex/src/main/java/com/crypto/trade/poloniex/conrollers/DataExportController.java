package com.crypto.trade.poloniex.conrollers;

import com.crypto.trade.poloniex.services.export.AnalyticsExportService;
import com.crypto.trade.poloniex.services.export.CandlesExportService;
import com.crypto.trade.poloniex.services.export.TicksExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/export")
public class DataExportController {

    @Autowired
    private TicksExportService poloniexTicksExportService;
    @Autowired
    private CandlesExportService candlesExportService;
    @Autowired
    private AnalyticsExportService analyticsExportService;

    @GetMapping("/ticks")
    public void exportTicks() {
        poloniexTicksExportService.exportData();
    }

    @GetMapping("/candles")
    public void exportCandles() {
        candlesExportService.exportData();
    }

    @GetMapping("/analytics")
    public void exportAnalytics() {
        analyticsExportService.exportData();
    }

    @GetMapping("/all")
    public void all() {
        poloniexTicksExportService.exportData();
        candlesExportService.exportData();
        analyticsExportService.exportData();
    }
}
