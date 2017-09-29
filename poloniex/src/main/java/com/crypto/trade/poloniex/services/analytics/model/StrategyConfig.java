package com.crypto.trade.poloniex.services.analytics.model;

import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import eu.verdelhan.ta4j.Order;
import lombok.Value;

import java.math.BigDecimal;

@Value(staticConstructor = "of")
public class StrategyConfig {

    private TimeFrame timeFrame;
    private BigDecimal volume;
    private Order.OrderType direction;
    private int recordsCount;
}
