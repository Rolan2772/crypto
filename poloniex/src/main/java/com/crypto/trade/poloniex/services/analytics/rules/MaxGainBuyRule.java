package com.crypto.trade.poloniex.services.analytics.rules;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import eu.verdelhan.ta4j.trading.rules.AbstractRule;

import java.util.stream.IntStream;

public class MaxGainBuyRule extends AbstractRule {

    private ClosePriceIndicator closePrice;

    private Decimal maxRecession;

    public MaxGainBuyRule(ClosePriceIndicator closePrice, Decimal maxRecession) {
        this.closePrice = closePrice;
        this.maxRecession = maxRecession.dividedBy(Decimal.HUNDRED);
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;
        if (tradingRecord != null) {
            Trade currentTrade = tradingRecord.getCurrentTrade();
            if (currentTrade.isOpened()) {
                Decimal entryPrice = currentTrade.getEntry().getPrice();
                Decimal currentPrice = closePrice.getValue(index);

                Decimal maxPrice = findMaxPrice(currentTrade.getEntry().getIndex(), index);
                Decimal maxGain = maxPrice.dividedBy(entryPrice);
                Decimal currentGain = currentPrice.dividedBy(entryPrice);
                satisfied = currentGain.isLessThan(maxGain.minus(maxRecession));
            }
        }
        traceIsSatisfied(index, satisfied);
        return satisfied;
    }

    private Decimal findMaxPrice(int entryIndex, int currentIndex) {
        return IntStream.rangeClosed(entryIndex, currentIndex)
                .mapToObj(closePrice::getValue)
                .max(Decimal::compareTo).orElse(Decimal.ZERO);
    }
}
