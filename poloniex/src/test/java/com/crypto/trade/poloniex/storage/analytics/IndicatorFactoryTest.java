package com.crypto.trade.poloniex.storage.analytics;

import com.crypto.trade.poloniex.services.analytics.indicators.CachedDoubleEMAIndicator;
import com.crypto.trade.poloniex.services.analytics.indicators.CachedTripleEMAIndicator;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.indicators.EMAIndicator;
import eu.verdelhan.ta4j.indicators.RSIIndicator;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class IndicatorFactoryTest {

    @Test
    public void closedPriceFactory() {
        List<Tick> candles = new ArrayList<>();
        IndicatorsStorage storage = new IndicatorsStorage();

        ClosePriceIndicator indicator = IndicatorFactory.createIndicator(IndicatorType.CLOSED_PRICE, candles, storage);

        assertNotNull(indicator);
        assertEquals(candles, indicator.getTimeSeries().getTickData());
        assertTrue(storage.getIndicators().isEmpty());
    }

    @Test
    public void rsiFactory() {
        List<Tick> candles = new ArrayList<>();
        IndicatorsStorage storage = new IndicatorsStorage();

        RSIIndicator indicator = IndicatorFactory.createIndicator(IndicatorType.RSI14, candles, storage);

        assertNotNull(indicator);
        assertEquals(candles, indicator.getTimeSeries().getTickData());
        assertEquals(1, storage.getIndicators().size());
        assertNotNull(storage.getIndicators().get(IndicatorType.CLOSED_PRICE));
    }

    @Test
    public void stochKFactory() {
        List<Tick> candles = new ArrayList<>();
        IndicatorsStorage storage = new IndicatorsStorage();

        StochasticOscillatorKIndicator indicator = IndicatorFactory.createIndicator(IndicatorType.STOCHK14, candles, storage);

        assertNotNull(indicator);
        assertEquals(candles, indicator.getTimeSeries().getTickData());
        assertTrue(storage.getIndicators().isEmpty());
    }

    @Test
    public void stochDFactory() {
        List<Tick> candles = new ArrayList<>();
        IndicatorsStorage storage = new IndicatorsStorage();

        StochasticOscillatorDIndicator indicator = IndicatorFactory.createIndicator(IndicatorType.STOCHD3, candles, storage);

        assertNotNull(indicator);
        assertEquals(candles, indicator.getTimeSeries().getTickData());
        assertEquals(1, storage.getIndicators().size());
        assertNotNull(storage.getIndicators().get(IndicatorType.STOCHK14));
    }

    @Test
    public void ema5Factory() {
        List<Tick> candles = new ArrayList<>();
        IndicatorsStorage storage = new IndicatorsStorage();

        EMAIndicator indicator = IndicatorFactory.createIndicator(IndicatorType.EMA5, candles, storage);

        assertNotNull(indicator);
        assertEquals(candles, indicator.getTimeSeries().getTickData());
        assertEquals(1, storage.getIndicators().size());
        assertNotNull(storage.getIndicators().get(IndicatorType.CLOSED_PRICE));
    }

    @Test
    public void ema90Factory() {
        List<Tick> candles = new ArrayList<>();
        IndicatorsStorage storage = new IndicatorsStorage();

        EMAIndicator indicator = IndicatorFactory.createIndicator(IndicatorType.EMA90, candles, storage);

        assertNotNull(indicator);
        assertEquals(candles, indicator.getTimeSeries().getTickData());
        assertEquals(1, storage.getIndicators().size());
        assertNotNull(storage.getIndicators().get(IndicatorType.CLOSED_PRICE));
    }

    @Test
    public void emaEma90Factory() {
        List<Tick> candles = new ArrayList<>();
        IndicatorsStorage storage = new IndicatorsStorage();

        EMAIndicator indicator = IndicatorFactory.createIndicator(IndicatorType.EMA_EMA90, candles, storage);

        assertNotNull(indicator);
        assertEquals(candles, indicator.getTimeSeries().getTickData());
        assertEquals(2, storage.getIndicators().size());
        assertNotNull(storage.getIndicators().get(IndicatorType.CLOSED_PRICE));
        assertNotNull(storage.getIndicators().get(IndicatorType.EMA90));
    }

    @Test
    public void emaEmaEma90Factory() {
        List<Tick> candles = new ArrayList<>();
        IndicatorsStorage storage = new IndicatorsStorage();

        EMAIndicator indicator = IndicatorFactory.createIndicator(IndicatorType.EMA_EMA_EMA90, candles, storage);

        assertNotNull(indicator);
        assertEquals(candles, indicator.getTimeSeries().getTickData());
        assertEquals(3, storage.getIndicators().size());
        assertNotNull(storage.getIndicators().get(IndicatorType.CLOSED_PRICE));
        assertNotNull(storage.getIndicators().get(IndicatorType.EMA90));
        assertNotNull(storage.getIndicators().get(IndicatorType.EMA_EMA90));
    }

    @Test
    public void dma90Factory() {
        List<Tick> candles = new ArrayList<>();
        IndicatorsStorage storage = new IndicatorsStorage();

        CachedDoubleEMAIndicator indicator = IndicatorFactory.createIndicator(IndicatorType.DMA90, candles, storage);

        assertNotNull(indicator);
        assertEquals(candles, indicator.getTimeSeries().getTickData());
        assertEquals(3, storage.getIndicators().size());
        assertNotNull(storage.getIndicators().get(IndicatorType.CLOSED_PRICE));
        assertNotNull(storage.getIndicators().get(IndicatorType.EMA90));
        assertNotNull(storage.getIndicators().get(IndicatorType.EMA_EMA90));
    }

    @Test
    public void tma90Factory() {
        List<Tick> candles = new ArrayList<>();
        IndicatorsStorage storage = new IndicatorsStorage();

        CachedTripleEMAIndicator indicator = IndicatorFactory.createIndicator(IndicatorType.TMA90, candles, storage);

        assertNotNull(indicator);
        assertEquals(candles, indicator.getTimeSeries().getTickData());
        assertEquals(4, storage.getIndicators().size());
        assertNotNull(storage.getIndicators().get(IndicatorType.CLOSED_PRICE));
        assertNotNull(storage.getIndicators().get(IndicatorType.EMA90));
        assertNotNull(storage.getIndicators().get(IndicatorType.EMA_EMA90));
        assertNotNull(storage.getIndicators().get(IndicatorType.EMA_EMA_EMA90));
    }

}
