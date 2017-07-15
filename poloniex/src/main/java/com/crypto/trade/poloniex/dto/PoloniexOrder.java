package com.crypto.trade.poloniex.dto;

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
    private ZonedDateTime requestTime = ZonedDateTime.now(ZoneId.of("GMT+0"));
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime executedTime;
    private Order sourceOrder;

    public PoloniexOrder(Long orderId, Order sourceOrder) {
        this.orderId = orderId;
        this.sourceOrder = sourceOrder;
    }
}
