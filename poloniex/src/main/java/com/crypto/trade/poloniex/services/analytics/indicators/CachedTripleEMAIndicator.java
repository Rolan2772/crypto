package com.crypto.trade.poloniex.services.analytics.indicators;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.indicators.CachedIndicator;
import eu.verdelhan.ta4j.indicators.trackers.EMAIndicator;

public class CachedTripleEMAIndicator extends CachedIndicator<Decimal> {

    private final EMAIndicator ema;
    private final EMAIndicator emaEma;
    private final EMAIndicator emaEmaEma;

    public CachedTripleEMAIndicator(Indicator<Decimal> indicator,
                                    EMAIndicator ema,
                                    EMAIndicator emaEma,
                                    EMAIndicator emaEmaEma) {
        super(indicator);
        this.ema = ema;
        this.emaEma = emaEma;
        this.emaEmaEma = emaEmaEma;
    }

    @Override
    protected Decimal calculate(int index) {
        return Decimal.THREE
                .multipliedBy(ema.getValue(index)
                        .minus(emaEma.getValue(index)))
                .plus(emaEmaEma.getValue(index));
    }
}
