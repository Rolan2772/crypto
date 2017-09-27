package com.crypto.trade.poloniex.storage.analytics;

import eu.verdelhan.ta4j.*;
import eu.verdelhan.ta4j.indicators.RSIIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import lombok.Value;

import java.util.List;
import java.util.function.Supplier;

@Value
public class IndicatorSupplier<T extends Indicator<Decimal>> implements Supplier<T> {

    private IndicatorType type;
    private List<Tick> candles;

    @Override
    public T get() {
        TimeSeries timeSeries = new BaseTimeSeries(candles);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        switch (type) {
            case CLOSED_PRICE:
                return (T) closePrice;
            case RSI14:
                return (T) new RSIIndicator(closePrice, 14);
            default:
                throw new IllegalArgumentException("Wrong indicator type: " + type);
        }
    }
}
