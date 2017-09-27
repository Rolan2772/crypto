package com.crypto.trade.poloniex.services.analytics.strategies;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.analytics.model.TripleEmaAnalytics;
import com.crypto.trade.poloniex.services.analytics.rules.LowerRule;
import com.crypto.trade.poloniex.services.analytics.rules.MaxGainBuyRule;
import com.crypto.trade.poloniex.services.analytics.rules.ModifiedStopGainRule;
import com.crypto.trade.poloniex.services.analytics.rules.UpperRule;
import eu.verdelhan.ta4j.BaseStrategy;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.trading.rules.CrossedDownIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.CrossedUpIndicatorRule;
import org.springframework.beans.factory.annotation.Autowired;

public class TripleEmaStrategyFactory {

    @Autowired
    private AnalyticsHelper analyticsHelper;

    public Strategy createRisingTripleEmaStrategyCorrected(CurrencyPair currencyPair, TimeFrame timeFrame) {
        TripleEmaAnalytics analytics = analyticsHelper.getTripleEmaAnalytics(currencyPair, timeFrame);

        Rule entryRule = new LowerRule(analytics.getTma90(), analytics.getDma90())
                .and(new LowerRule(analytics.getTma90(), analytics.getEma90()))
                .and(new CrossedUpIndicatorRule(analytics.getEma5(), analytics.getTma90()));

        Rule exitRule = new UpperRule(analytics.getTma90(), analytics.getEma90())
                .and(new CrossedDownIndicatorRule(analytics.getEma5(), analytics.getTma90()))
                .and(new ModifiedStopGainRule(analytics.getClosePrice(), Decimal.ONE));

        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(270);

        return strategy;
    }

    public Strategy createFallingTripleEmaStrategy(CurrencyPair currencyPair, TimeFrame timeFrame) {
        TripleEmaAnalytics analytics = analyticsHelper.getTripleEmaAnalytics(currencyPair, timeFrame);

        Rule entryRule = new LowerRule(analytics.getTma90(), analytics.getDma90())
                .and(new LowerRule(analytics.getTma90(), analytics.getEma90()))
                .and(new CrossedDownIndicatorRule(analytics.getEma5(), analytics.getTma90()));

        Rule exitRule = new UpperRule(analytics.getTma90(), analytics.getEma90())
                .and(new CrossedUpIndicatorRule(analytics.getEma5(), analytics.getTma90()))
                .and(new ModifiedStopGainRule(analytics.getClosePrice(), Decimal.ONE));

        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(270);

        return strategy;
    }

    public Strategy createRisingTripleEmaStrategy2(CurrencyPair currencyPair, TimeFrame timeFrame) {
        TripleEmaAnalytics analytics = analyticsHelper.getTripleEmaAnalytics(currencyPair, timeFrame);

        Rule entryRule = new UpperRule(analytics.getTma90(), analytics.getEma90())
                .and(new CrossedUpIndicatorRule(analytics.getEma5(), analytics.getTma90()));

        Rule exitRule = new LowerRule(analytics.getTma90(), analytics.getDma90())
                .and(new LowerRule(analytics.getTma90(), analytics.getEma90()))
                .and(new CrossedDownIndicatorRule(analytics.getEma5(), analytics.getTma90()))
                .and(new ModifiedStopGainRule(analytics.getClosePrice(), Decimal.ONE))
                .and(new MaxGainBuyRule(analytics.getClosePrice(), Decimal.ONE));

        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(270);

        return strategy;
    }


}
