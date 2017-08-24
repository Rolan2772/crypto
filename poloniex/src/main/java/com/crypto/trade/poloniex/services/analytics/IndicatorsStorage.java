package com.crypto.trade.poloniex.services.analytics;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
public class IndicatorsStorage {

    private Map<IndicatorType, Indicator<Decimal>> indicators = new HashMap<>();

    public <T extends Indicator<Decimal>> T getIndicator(IndicatorType indicatorType, Indicator<Decimal> defaultValue) {
        indicators.putIfAbsent(indicatorType, defaultValue);
        return (T) indicators.get(indicatorType);
    }
}
