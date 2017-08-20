package com.crypto.trade.poloniex.storage;

import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.TradingRecord;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Value
@EqualsAndHashCode(of = {"id"})
public class PoloniexTradingRecord {

    private int id;
    private String strategyName;
    private TradingRecord tradingRecord;
    private AtomicBoolean processing = new AtomicBoolean();
    // @TODO: create poloniex trade with related orders
    private List<PoloniexOrder> orders = new ArrayList<>();

    public PoloniexTradingRecord(int id, String strategyName, Order.OrderType direction) {
        this.id = id;
        this.strategyName = strategyName;
        this.tradingRecord = new TradingRecord(direction);
    }

    public void addPoloniexOrder(PoloniexOrder order) {
        orders.add(order);
    }

    public void setProcessed() {
        this.processing.set(false);
    }
}
