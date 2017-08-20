package com.crypto.trade.poloniex.services.trade;

import eu.verdelhan.ta4j.Order;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;

@Value
@AllArgsConstructor
public class TradeResult {

    private BigDecimal entrySpent;
    private BigDecimal netExitGain;
    private BigDecimal grossExitGain;
    private Order.OrderType direction;
    // @TODO: add TotalResult data structure, and move volume and trades count there
    private BigDecimal volume;
    private Integer tradesCount;

    public TradeResult(Order.OrderType direction, BigDecimal volume) {
        this.entrySpent = BigDecimal.ZERO;
        this.netExitGain = BigDecimal.ZERO;
        this.grossExitGain = BigDecimal.ZERO;
        this.direction = direction;
        this.volume = volume;
        this.tradesCount = 0;
    }

    public TradeResult(Order.OrderType direction) {
        this(direction, BigDecimal.ZERO);
    }
}
