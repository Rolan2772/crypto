package com.crypto.trade.poloniex.services.analytics.model;

import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Tick;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;

@Value
@RequiredArgsConstructor(staticName = "of")
public class TradeData {

    private Tick tick;
    private int index;
    private Order.OrderType direction;
    private BigDecimal volume;
}
