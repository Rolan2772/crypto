package com.crypto.trade.poloniex.conrollers;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.export.DataConversionService;
import com.crypto.trade.poloniex.storage.CandlesStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/trade")
public class TradeApiController {

    @Autowired
    private DataConversionService ordersExportService;
    @Autowired
    private CandlesStorage candlesStorage;

    @GetMapping("/{currency}/orders")
    public String getOrders(@PathVariable CurrencyPair currency) {
        return candlesStorage.getData(currency)
                .stream()
                .map(timeFrameStorage -> {
                    return timeFrameStorage.getTimeFrame().getDisplayName() + "\n" +
                            ordersExportService.convert(timeFrameStorage).toString();
                })
                .collect(Collectors.joining("\n\n"));
    }

    @GetMapping("/loadOrders")
    public void loadOrders(@RequestParam String fileName) {
        // @TODO: load previos execution orders
    }
}