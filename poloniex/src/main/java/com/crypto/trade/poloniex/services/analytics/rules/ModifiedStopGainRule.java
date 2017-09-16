package com.crypto.trade.poloniex.services.analytics.rules;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import eu.verdelhan.ta4j.trading.rules.AbstractRule;

public class ModifiedStopGainRule extends AbstractRule {

    /**
     * The close price indicator
     */
    private ClosePriceIndicator closePrice;

    /**
     * The gain ratio threshold (e.g. 1.03 for 3%)
     */
    private Decimal buyThreshold;
    private Decimal sellThreshold;

    /**
     * Constructor.
     *
     * @param closePrice     the close price indicator
     * @param gainPercentage the gain percentage
     */
    public ModifiedStopGainRule(ClosePriceIndicator closePrice, Decimal gainPercentage) {
        this.closePrice = closePrice;
        this.buyThreshold = Decimal.HUNDRED.plus(gainPercentage).dividedBy(Decimal.HUNDRED);
        this.sellThreshold = Decimal.HUNDRED.minus(gainPercentage).dividedBy(Decimal.HUNDRED);
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;
        // No trading history or no trade opened, no gain
        if (tradingRecord != null) {
            Trade currentTrade = tradingRecord.getCurrentTrade();
            if (currentTrade.isOpened()) {
                Decimal entryPrice = currentTrade.getEntry().getPrice();
                Decimal currentPrice = closePrice.getValue(index);
                if (currentTrade.getEntry().isBuy()) {
                    Decimal threshold = entryPrice.multipliedBy(buyThreshold);
                    satisfied = currentPrice.isGreaterThanOrEqual(threshold);
                } else {
                    Decimal threshold = entryPrice.multipliedBy(sellThreshold);
                    satisfied = currentPrice.isLessThanOrEqual(threshold);
                }
            }
        }
        traceIsSatisfied(index, satisfied);
        return satisfied;
    }
}
