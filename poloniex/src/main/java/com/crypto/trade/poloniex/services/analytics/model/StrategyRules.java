package com.crypto.trade.poloniex.services.analytics.model;

import eu.verdelhan.ta4j.Decimal;
import lombok.Value;

@Value(staticConstructor = "of")
public class StrategyRules {

    private Decimal stopGain;
    private Decimal maxGainCorridor;
}
