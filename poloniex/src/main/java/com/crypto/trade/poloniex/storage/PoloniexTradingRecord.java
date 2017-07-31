package com.crypto.trade.poloniex.storage;

import com.crypto.trade.poloniex.dto.PoloniexOrder;
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
    private List<PoloniexOrder> orders = new ArrayList<>();

    public void addPoloniexOrder(PoloniexOrder order) {
        orders.add(order);
    }

    public void setProcessed() {
        this.processing.set(false);
    }
}
