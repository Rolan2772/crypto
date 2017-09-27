package com.crypto.trade.poloniex.services.analytics.model;

import eu.verdelhan.ta4j.indicators.EMAIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import lombok.Value;

@Value(staticConstructor = "of")
public class TrendAnalytics {

    private ClosePriceIndicator closePrice;
    private EMAIndicator ema5;
    private EMAIndicator ema90;
    private EMAIndicator ema100;
}
