package com.crypto.trade.poloniex.storage.analytics;

import com.crypto.trade.poloniex.services.analytics.indicators.CachedDoubleEMAIndicator;
import com.crypto.trade.poloniex.services.analytics.indicators.CachedTripleEMAIndicator;
import eu.verdelhan.ta4j.*;
import eu.verdelhan.ta4j.indicators.EMAIndicator;
import eu.verdelhan.ta4j.indicators.RSIIndicator;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;

import java.util.List;
import java.util.function.Supplier;

import static com.crypto.trade.poloniex.storage.analytics.IndicatorType.*;

public class IndicatorFactory {

    @SuppressWarnings("unchecked")
    public static <T extends Indicator<Decimal>> T createIndicator(IndicatorType indicatorType, List<Tick> candles, IndicatorsStorage storage) {
        TimeSeries timeSeries = new BaseTimeSeries(candles);
        switch (indicatorType) {
            case CLOSED_PRICE:
                return (T) new ClosePriceIndicator(timeSeries);
            case RSI14:
                return (T) new RSIIndicator(getClosePriceIndicator(candles, storage), 14);
            case STOCHK14:
                return (T) new StochasticOscillatorKIndicator(timeSeries, 14);
            case STOCHD3:
                return (T) new StochasticOscillatorDIndicator(storage.getIndicator(STOCHK14, createSupplier(STOCHK14, candles, storage)));
            case EMA5:
                return (T) new EMAIndicator(getClosePriceIndicator(candles, storage), 5);
            case EMA90:
                return (T) new EMAIndicator(getClosePriceIndicator(candles, storage), 90);
            case EMA100:
                return (T) new EMAIndicator(getClosePriceIndicator(candles, storage), 100);
            case EMA540:
                return (T) new EMAIndicator(getClosePriceIndicator(candles, storage), 540);
            case EMA_EMA90:
                return (T) new EMAIndicator(storage.getIndicator(EMA90, createSupplier(EMA90, candles, storage)), 90);
            case DMA90:
                return (T) new CachedDoubleEMAIndicator(getClosePriceIndicator(candles, storage),
                        storage.getIndicator(EMA90, createSupplier(EMA90, candles, storage)),
                        storage.getIndicator(EMA_EMA90, createSupplier(EMA_EMA90, candles, storage)));
            case EMA_EMA_EMA90:
                return (T) new EMAIndicator(storage.getIndicator(EMA_EMA90, createSupplier(EMA_EMA90, candles, storage)), 90);
            case TMA90:
                return (T) new CachedTripleEMAIndicator(getClosePriceIndicator(candles, storage),
                        storage.getIndicator(EMA90, createSupplier(EMA90, candles, storage)),
                        storage.getIndicator(EMA_EMA90, createSupplier(EMA_EMA90, candles, storage)),
                        storage.getIndicator(EMA_EMA_EMA90, createSupplier(EMA_EMA_EMA90, candles, storage)));
            default:
                throw new IllegalArgumentException("Wrong indicator type: " + indicatorType);
        }
    }

    public static <T> Supplier<T> createSupplier(IndicatorType type, List<Tick> candles, IndicatorsStorage storage) {
        return () -> IndicatorFactory.createIndicator(type, candles, storage);
    }

    private static ClosePriceIndicator getClosePriceIndicator(List<Tick> candles, IndicatorsStorage storage) {
        return storage.getIndicator(CLOSED_PRICE,
                () -> IndicatorFactory.createIndicator(CLOSED_PRICE, candles, storage));
    }
}
