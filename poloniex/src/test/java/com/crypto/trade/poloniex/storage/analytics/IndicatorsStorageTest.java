package com.crypto.trade.poloniex.storage.analytics;

import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.indicators.EMAIndicator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

public class IndicatorsStorageTest {

    @Test
    public void indicatorCache() {
        IndicatorsStorage storage = new IndicatorsStorage();
        List<Tick> candles = new ArrayList<>();
        IndicatorType type = IndicatorType.EMA90;

        EMAIndicator created = storage.getIndicator(type, IndicatorFactory.createSupplier(type, candles, storage));
        EMAIndicator cached = storage.getIndicator(type, IndicatorFactory.createSupplier(type, candles, storage));

        assertTrue(created == cached);
    }

    @Test
    public void dependentIndicatorCache() {
        IndicatorsStorage storage = new IndicatorsStorage();
        List<Tick> candles = new ArrayList<>();
        IndicatorType type = IndicatorType.DMA90;

        IntStream.range(0, 5).forEach(index ->
                storage.getIndicator(type, IndicatorFactory.createSupplier(type, candles, storage)));

        assertEquals(4, storage.getIndicators().size());
        assertNotNull(storage.getIndicators().get(IndicatorType.CLOSED_PRICE));
        assertNotNull(storage.getIndicators().get(IndicatorType.EMA90));
        assertNotNull(storage.getIndicators().get(IndicatorType.EMA_EMA90));
        assertNotNull(storage.getIndicators().get(IndicatorType.DMA90));
    }
}
