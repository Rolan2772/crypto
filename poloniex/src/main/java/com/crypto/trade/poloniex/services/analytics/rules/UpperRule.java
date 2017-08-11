package com.crypto.trade.poloniex.services.analytics.rules;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.trading.rules.AbstractRule;

public class UpperRule extends AbstractRule {

    private Indicator<Decimal> first;
    private Indicator<Decimal> second;
    private int depth;

    public UpperRule(Indicator<Decimal> first, Indicator<Decimal> second) {
        this.first = first;
        this.second = second;
        this.depth = 0;
    }

    public UpperRule(Indicator<Decimal> first, Indicator<Decimal> second, int depth) {
        this.first = first;
        this.second = second;
        this.depth = depth;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;
        int currIndex = index - depth;
        if (currIndex > -1) {
            satisfied = first.getValue(currIndex).isGreaterThan(second.getValue(currIndex));
        }
        traceIsSatisfied(currIndex, satisfied);
        return satisfied;
    }
}
