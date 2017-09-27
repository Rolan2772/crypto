package com.crypto.trade.poloniex.storage.model;

import com.crypto.trade.poloniex.services.analytics.TradingAction;
import com.crypto.trade.poloniex.services.utils.CalculationsUtils;
import com.crypto.trade.poloniex.services.utils.DateTimeUtils;
import eu.verdelhan.ta4j.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class PoloniexOrder {

    private Long orderId;
    private int index;
    private TradingAction action;
    private Order sourceOrder;
    private BigDecimal fee = CalculationsUtils.FEE_PERCENT;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime tradeTime;

    public PoloniexOrder(Long orderId, Order sourceOrder, int index, TradingAction action) {
        this.orderId = orderId;
        this.tradeTime = DateTimeUtils.now();
        this.sourceOrder = sourceOrder;
        this.index = index;
        this.action = action;
    }
}
