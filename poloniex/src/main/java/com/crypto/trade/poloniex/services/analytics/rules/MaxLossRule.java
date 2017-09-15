package com.crypto.trade.poloniex.services.analytics.rules;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import eu.verdelhan.ta4j.trading.rules.AbstractRule;

import java.util.stream.IntStream;

public class MaxLossRule extends AbstractRule {

    private ClosePriceIndicator closePrice;

    private Decimal maxRecession;

    public MaxLossRule(ClosePriceIndicator closePrice, Decimal maxRecession) {
        this.closePrice = closePrice;
        this.maxRecession = maxRecession.dividedBy(Decimal.HUNDRED);
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;
        if (tradingRecord != null) {
            Trade currentTrade = tradingRecord.getCurrentTrade();
            if (currentTrade.isOpened()) {
                Order entryOrder = currentTrade.getEntry();
                Decimal entryPrice = entryOrder.getPrice();
                Decimal currentPrice = closePrice.getValue(index);

                Decimal minPrice = findMinPrice(entryOrder.getIndex(), index);
                Decimal maxGain = entryPrice.dividedBy(minPrice);
                Decimal currentGain = entryPrice.dividedBy(currentPrice);
                satisfied = currentGain.isLessThan(maxGain.minus(maxRecession));
            }
        }
        traceIsSatisfied(index, satisfied);
        return satisfied;
    }

    private Decimal findMinPrice(int entryIndex, int currentIndex) {
        return IntStream.rangeClosed(entryIndex, currentIndex)
                .mapToObj(index -> closePrice.getValue(index))
                .min(Decimal::compareTo).orElse(Decimal.ZERO);
    }
}
