package com.crypto.trade.utils;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Trade;

public class TestOrderFactory {

    public static Order createEntryOrder(Decimal price, Decimal amount) {
        return createEntryOrder(Order.OrderType.BUY, price, amount);
    }

    public static Order createEntryOrder(Order.OrderType orderType, Decimal price, Decimal amount) {
        Trade trade = new Trade(orderType);
        return trade.operate(0, price, amount);
    }
}
