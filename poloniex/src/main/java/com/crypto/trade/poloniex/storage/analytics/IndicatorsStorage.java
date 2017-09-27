package com.crypto.trade.poloniex.storage.analytics;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Value
public class IndicatorsStorage {

    private Map<IndicatorType, Indicator<Decimal>> indicators = new HashMap<>();

    public <T extends Indicator<Decimal>> T getIndicator(IndicatorType indicatorType, Supplier<T> factory) {
        indicators.computeIfAbsent(indicatorType, key -> factory.get());
        return (T) indicators.get(indicatorType);
    }

    public void setIndicator(IndicatorType indicatorType, Indicator<Decimal> value) {
        indicators.put(indicatorType, value);
    }

}
