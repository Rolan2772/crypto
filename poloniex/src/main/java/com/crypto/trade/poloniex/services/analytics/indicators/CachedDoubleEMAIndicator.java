package com.crypto.trade.poloniex.services.analytics.indicators;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.indicators.CachedIndicator;
import eu.verdelhan.ta4j.indicators.trackers.EMAIndicator;

public class CachedDoubleEMAIndicator extends CachedIndicator<Decimal> {


    private final EMAIndicator ema;
    private final EMAIndicator emaEma;

    public CachedDoubleEMAIndicator(Indicator<Decimal> indicator,
                                    EMAIndicator ema,
                                    EMAIndicator emaEma) {
        super(indicator);
        this.ema = ema;
        this.emaEma = emaEma;
    }

    @Override
    protected Decimal calculate(int index) {
        return ema.getValue(index).multipliedBy(Decimal.TWO)
                .minus(emaEma.getValue(index));
    }
}
