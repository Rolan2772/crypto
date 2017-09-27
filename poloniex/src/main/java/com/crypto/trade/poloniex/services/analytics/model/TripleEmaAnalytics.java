package com.crypto.trade.poloniex.services.analytics.model;

import com.crypto.trade.poloniex.services.analytics.indicators.CachedDoubleEMAIndicator;
import com.crypto.trade.poloniex.services.analytics.indicators.CachedTripleEMAIndicator;
import eu.verdelhan.ta4j.indicators.EMAIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import lombok.Value;

@Value(staticConstructor = "of")
public class TripleEmaAnalytics {

    private ClosePriceIndicator closePrice;
    private EMAIndicator ema5;
    private EMAIndicator ema90;
    private EMAIndicator emaEma90;
    private CachedDoubleEMAIndicator dma90;
    private EMAIndicator emaEmaEma90;
    private CachedTripleEMAIndicator tma90;
}
