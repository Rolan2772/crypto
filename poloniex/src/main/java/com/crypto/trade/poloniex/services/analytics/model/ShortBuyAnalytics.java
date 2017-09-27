package com.crypto.trade.poloniex.services.analytics.model;

import eu.verdelhan.ta4j.indicators.EMAIndicator;
import eu.verdelhan.ta4j.indicators.RSIIndicator;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import lombok.Value;

@Value(staticConstructor = "of")
public class ShortBuyAnalytics {

    private ClosePriceIndicator closePrice;
    private RSIIndicator rsi;
    private EMAIndicator ema90;
    private EMAIndicator ema540;
    private StochasticOscillatorKIndicator stochK;
    private StochasticOscillatorDIndicator stochD;
}
