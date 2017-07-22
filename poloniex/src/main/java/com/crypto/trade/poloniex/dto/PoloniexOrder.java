package com.crypto.trade.poloniex.dto;

import com.crypto.trade.poloniex.services.analytics.TradingAction;
import eu.verdelhan.ta4j.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class PoloniexOrder {

    private Long orderId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime requestTime;
    private Order sourceOrder;
    private TradingAction action;
    private int index;

    public PoloniexOrder(Long orderId, Order sourceOrder, int index, TradingAction action) {
        this.orderId = orderId;
        this.requestTime = ZonedDateTime.now(ZoneId.of("GMT+0"));
        this.sourceOrder = sourceOrder;
        this.index = index;
        this.action = action;
    }
}
