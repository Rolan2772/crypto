package com.crypto.trade.poloniex.services.analytics.strategies;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.analytics.model.StrategyRules;
import com.crypto.trade.poloniex.services.analytics.model.TrendAnalytics;
import com.crypto.trade.poloniex.services.analytics.rules.*;
import eu.verdelhan.ta4j.BaseStrategy;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.trading.rules.NotRule;
import eu.verdelhan.ta4j.trading.rules.OrRule;
import org.springframework.beans.factory.annotation.Autowired;

public class TrendStrategyFactory {

    @Autowired
    private AnalyticsHelper analyticsHelper;

    public Strategy createRisingTrendStrategy(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return createRisingTrendStrategy(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.ZERO));
    }

    public Strategy createRisingTrendStrategy(CurrencyPair currencyPair, TimeFrame timeFrame, StrategyRules strategyRules) {
        TrendAnalytics analytics = analyticsHelper.getRisingAnalytics(currencyPair, timeFrame);

        Rule entryRule = createBuyEntryRule(analytics);
        Rule exitRule = createBuyExitRule(analytics)
                .and(new ModifiedStopGainRule(analytics.getClosePrice(), strategyRules.getStopGain()));

        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(100);

        return strategy;
    }

    private Rule createBuyEntryRule(TrendAnalytics analytics) {
        // ema90[0] >= ema90[-1] and ma05 > ma100
        Rule trendUp = new NotRule(new FallingDownIndicatorRule(analytics.getEma90()))
                .and(new UpperRule(analytics.getEma5(), analytics.getEma100()));
        // ema90[-1] >= ema90[-2] and ema05[-1] < ma100[-1]
        Rule buySignal1 = new NotRule(new FallingDownIndicatorRule(analytics.getEma90(), 1))
                .and(new LowerRule(analytics.getEma5(), analytics.getEma100(), 1));
        // ema90[-1] <= ema90[-2] and ema05[-1] < ema100[-1]
        Rule buySignal2 = new NotRule(new RisingUpIndicatorRule(analytics.getEma90(), 1))
                .and(new LowerRule(analytics.getEma5(), analytics.getEma100(), 1));

        Rule entry1 = trendUp.and(buySignal1);
        Rule entry2 = trendUp.and(new NotRule(buySignal1)).and(buySignal2);

        return entry1.or(entry2);
    }

    private Rule createBuyExitRule(TrendAnalytics analytics) {
        // Exit rule
        // ema90[0] <= ema90[-1] and ema05 < ema100
        Rule trendDown = new NotRule(new RisingUpIndicatorRule(analytics.getEma90()))
                .and(new LowerRule(analytics.getEma5(), analytics.getEma100()));
        // ema90[0] < ema90[-1] and ema05 > ema100
        Rule trendPreDown = new FallingDownIndicatorRule(analytics.getEma90())
                .and(new UpperRule(analytics.getEma5(), analytics.getEma100()));
        // ema90[-1] >= ema90[-2] and ema05[-1] > ema100[-1]
        Rule exit1 = new NotRule(new FallingDownIndicatorRule(analytics.getEma90(), 1))
                .and(new UpperRule(analytics.getEma5(), analytics.getEma100(), 1));

        return new OrRule(trendDown, trendPreDown)
                .and(exit1);
    }

    public Strategy createModifiedRisingTrendStrategy(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return createRisingTrendStrategy(currencyPair, timeFrame, StrategyRules.of(Decimal.valueOf(40), Decimal.ZERO));
    }

    public Strategy createFallingTrendStrategy(CurrencyPair currencyPair, TimeFrame timeFrame) {
        TrendAnalytics analytics = analyticsHelper.getRisingAnalytics(currencyPair, timeFrame);

        // ema90[0] >= ema90[-1] and ma05 > ma100
        Rule trendUp = new NotRule(new FallingDownIndicatorRule(analytics.getEma90()))
                .and(new UpperRule(analytics.getEma5(), analytics.getEma100()));
        // ema90[-1] >= ema90[-2] and ema05[-1] < ma100[-1]
        Rule buySignal1 = new NotRule(new FallingDownIndicatorRule(analytics.getEma90(), 1))
                .and(new LowerRule(analytics.getEma5(), analytics.getEma100(), 1));
        // ema90[-1] <= ema90[-2] and ema05[-1] < ema100[-1]
        Rule buySignal2 = new NotRule(new RisingUpIndicatorRule(analytics.getEma90(), 1))
                .and(new LowerRule(analytics.getEma5(), analytics.getEma100(), 1));

        Rule entry1 = trendUp.and(buySignal1);
        Rule entry2 = trendUp.and(new NotRule(buySignal1)).and(buySignal2);

        Rule entryRule = entry1.or(entry2).and(new ModifiedStopGainRule(analytics.getClosePrice(), Decimal.ONE));

        // Exit rule
        // ema90[0] <= ema90[-1] and ema05 < ema100
        Rule trendDown = new NotRule(new RisingUpIndicatorRule(analytics.getEma90()))
                .and(new LowerRule(analytics.getEma5(), analytics.getEma100()));
        // ema90[0] < ema90[-1] and ema05 > ema100
        Rule trendPreDown = new FallingDownIndicatorRule(analytics.getEma90())
                .and(new UpperRule(analytics.getEma5(), analytics.getEma100()));
        // ema90[-1] >= ema90[-2] and ema05[-1] > ema100[-1]
        Rule exit1 = new NotRule(new FallingDownIndicatorRule(analytics.getEma90(), 1))
                .and(new UpperRule(analytics.getEma5(), analytics.getEma100(), 1));

        Rule exitRule = new OrRule(trendDown, trendPreDown)
                .and(exit1);

        Strategy strategy = new BaseStrategy(exitRule, entryRule);
        strategy.setUnstablePeriod(100);

        return strategy;
    }
}
