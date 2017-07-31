package com.crypto.trade.poloniex.conrollers;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.storage.PoloniexOrder;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.analytics.TradingAction;
import com.crypto.trade.poloniex.services.trade.TradingService;
import com.crypto.trade.poloniex.storage.CandlesStorage;
import com.crypto.trade.poloniex.storage.PoloniexStrategy;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TradingRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/trade")
public class TradeApiController {

    @Autowired
    private TradingService tradingService;
    @Autowired
    private CandlesStorage candlesStorage;
    @Autowired
    private PoloniexProperties properties;

    @GetMapping("/buy")
    public PoloniexOrder buy() {
        return tradingService.placeOrder(new TradingRecord(), 0, TradingAction.SHOULD_ENTER, properties.getTradeConfig().getMinBtcTradeAmount(), false).orElse(new PoloniexOrder(0L, null, 0, TradingAction.ENTERED));
    }

    @GetMapping("/sell")
    public PoloniexOrder buy(@RequestParam(required = false, defaultValue = "0.094534523") BigDecimal price) {
        TradingRecord tradingRecord = new TradingRecord();
        tradingRecord.enter(0, Decimal.valueOf("0.02342523"), Decimal.valueOf("0.009975"));
        return tradingService.placeOrder(tradingRecord, 1, TradingAction.SHOULD_ENTER, properties.getTradeConfig().getMinBtcTradeAmount(), false).orElse(new PoloniexOrder(0L, null, 0, TradingAction.EXITED));
    }

    @GetMapping("/cancel")
    public String getOrders(@RequestParam("orderId") Long orderId) {
        return tradingService.cancelOrder(new PoloniexOrder(orderId, null, 0, TradingAction.CANCELLED));
    }

    @GetMapping("/orders")
    public Map<TimeFrame, List<PoloniexStrategy>> getOrders() {
        return Arrays.stream(TimeFrame.values()).collect(Collectors.toMap(timeFrame -> timeFrame, timeFrame -> candlesStorage.getActiveStrategies(CurrencyPair.BTC_ETH, timeFrame)));
    }
}