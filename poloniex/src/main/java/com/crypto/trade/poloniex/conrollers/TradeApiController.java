package com.crypto.trade.poloniex.conrollers;

import com.crypto.trade.poloniex.dto.PoloniexOrder;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.analytics.TradingAction;
import com.crypto.trade.poloniex.services.integration.TradingService;
import com.crypto.trade.poloniex.storage.TickersStorage;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TradingRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/trade")
public class TradeApiController {

    @Autowired
    private TradingService tradingService;
    @Autowired
    private TickersStorage tickersStorage;

    @GetMapping("/buy")
    public PoloniexOrder buy() {
        return tradingService.placeOrder(new TradingRecord(), 0, TradingAction.SHOULD_ENTER, false).orElse(new PoloniexOrder(0L, null));
    }

    @GetMapping("/sell")
    public PoloniexOrder buy(@RequestParam(required = false, defaultValue = "0.094534523") BigDecimal price) {
        TradingRecord tradingRecord = new TradingRecord();
        tradingRecord.enter(0, Decimal.valueOf("0.02342523"), Decimal.valueOf("0.009975"));
        return tradingService.placeOrder(tradingRecord, 1, TradingAction.SHOULD_ENTER, false).orElse(new PoloniexOrder(0L, null));
    }

    @GetMapping("/cancel")
    public String getOrders(@RequestParam("orderId") Long orderId) {
        return tradingService.cancelOrder(new PoloniexOrder(orderId, null));
    }

    @GetMapping("/orders")

    public ConcurrentMap<TimeFrame, Set<PoloniexOrder>> getOrders() {
        return tickersStorage.getOrders();
    }
}
