package com.crypto.trade.poloniex.storage.analytics;

import com.crypto.trade.poloniex.config.properties.PoloniexProperties;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.storage.CandlesStorage;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.indicators.RecursiveCachedIndicator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AnalyticsStorage {

    @Autowired
    private PoloniexProperties properties;
    @Autowired
    private CandlesStorage candlesStorage;

    private Map<CurrencyPair, Map<TimeFrame, IndicatorsStorage>> cache = new EnumMap<>(CurrencyPair.class);

    @PostConstruct
    public void postConstruct() {
        properties.getCurrencies().forEach(currencyPair -> {
            for (TimeFrame timeFrame : TimeFrame.values()) {
                cache.compute(currencyPair, (key, value) -> {
                    Map<TimeFrame, IndicatorsStorage> indicators = Optional.ofNullable(value)
                            .orElseGet(() -> new EnumMap<>(TimeFrame.class));
                    indicators.put(timeFrame, new IndicatorsStorage());
                    return indicators;
                });
            }
        });
    }

    public <T extends Indicator<Decimal>> T getIndicator(CurrencyPair currencyPair,
                                                         TimeFrame timeFrame,
                                                         IndicatorType type) {
        IndicatorsStorage storage = cache.get(currencyPair).get(timeFrame);
        List<Tick> candles = candlesStorage.getData1(currencyPair).get(timeFrame).getCandles();
        IndicatorSupplier<T> factory = new IndicatorSupplier<>(type, candles);
        return storage.getIndicator(type, factory);
    }

    public void cacheIndex(int index) {
        cache.values()
                .stream()
                .flatMap(timeFrames -> timeFrames.values().stream())
                .flatMap(indicatorStorage -> indicatorStorage.getIndicators().values().stream())
                .filter(indicator -> indicator instanceof RecursiveCachedIndicator)
                .forEach(indicator -> indicator.getValue(index));
    }
}
