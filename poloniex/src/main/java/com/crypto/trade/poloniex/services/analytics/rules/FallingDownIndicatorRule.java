package com.crypto.trade.poloniex.services.analytics.rules;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.trading.rules.AbstractRule;

public class FallingDownIndicatorRule extends AbstractRule {

    private Indicator<Decimal> indicator;
    private int depth = 1;

    public FallingDownIndicatorRule(Indicator<Decimal> indicator) {
        this.indicator = indicator;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;
        if (index >= depth) {
            indicator.getValue(index - 1).isGreaterThan(indicator.getValue(index));
        }
        traceIsSatisfied(index, satisfied);
        return satisfied;
    }
}
