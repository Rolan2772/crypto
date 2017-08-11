package com.crypto.trade.poloniex.services.analytics.rules;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.trading.rules.AbstractRule;

public class RisingUpIndicatorRule extends AbstractRule {

    private Indicator<Decimal> indicator;
    private int depth;

    public RisingUpIndicatorRule(Indicator<Decimal> indicator) {
        this.indicator = indicator;
        this.depth = 0;
    }

    public RisingUpIndicatorRule(Indicator<Decimal> indicator, int depth) {
        this.indicator = indicator;
        this.depth = depth;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean isSatisfied = false;
        int prevIndex = index - depth - 1;
        int currIndex = index - depth;
        if (prevIndex > -1) {
            isSatisfied = indicator.getValue(prevIndex).isLessThan(indicator.getValue(currIndex));
        }
        log.trace("{}#isSatisfied({}/{}): {}", getClass().getSimpleName(), prevIndex, currIndex, isSatisfied);
        return isSatisfied;
    }
}
