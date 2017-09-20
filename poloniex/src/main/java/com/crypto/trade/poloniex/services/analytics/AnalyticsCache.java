package com.crypto.trade.poloniex.services.analytics;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.indicators.RecursiveCachedIndicator;

import java.util.Collection;
import java.util.EnumMap;

public class AnalyticsCache {

    private EnumMap<TimeFrame, IndicatorsStorage> cache = new EnumMap<>(TimeFrame.class);

    public AnalyticsCache() {
        for (TimeFrame timeFrame : TimeFrame.values()) {
            cache.put(timeFrame, new IndicatorsStorage());
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Indicator<Decimal>> T getIndicator(TimeFrame timeFrame, IndicatorType type, Indicator<Decimal> defaultValue) {
        return (T) cache.get(timeFrame).getIndicator(type, defaultValue);
    }

    public void cacheIndex(int index) {
        cache.values()
                .stream()
                .map(storage -> storage.getIndicators().values())
                .flatMap(Collection::stream)
                .filter(indicator -> indicator instanceof RecursiveCachedIndicator)
                .forEach(indicator -> indicator.getValue(index));
    }
}
