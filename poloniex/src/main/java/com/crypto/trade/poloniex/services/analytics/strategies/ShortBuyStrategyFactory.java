package com.crypto.trade.poloniex.services.analytics.strategies;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.analytics.model.ShortBuyAnalytics;
import com.crypto.trade.poloniex.services.analytics.model.StrategyRules;
import com.crypto.trade.poloniex.services.analytics.rules.*;
import eu.verdelhan.ta4j.BaseStrategy;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.trading.rules.CrossedDownIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.CrossedUpIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;
import org.springframework.beans.factory.annotation.Autowired;

// TODO: add ema 90 up on 1hour candles
// EMA90 angle should be as big as possible || em60 should be distant from ema 90 1hour
// One more Gaussian waves
// one more Pivot indicator
public class ShortBuyStrategyFactory {

    @Autowired
    private AnalyticsHelper analyticsHelper;

    /**
     * RSI 14, StochasticK 14, StochasticD 3
     * Buy on RSI < 20, K intersects D, K < 20
     * Sell +1%
     * Max gain 10%
     */
    public Strategy createShortBuyStrategy(CurrencyPair currencyPair, TimeFrame timeFrame) {
        ShortBuyAnalytics analytics = analyticsHelper.getShortBuyAnalytics(currencyPair, timeFrame);

        // Entry rule
        Rule entryRule = createBuyEntryRule(analytics);

        // Exit rule
        Rule exitRule = new ModifiedStopGainRule(analytics.getClosePrice(), Decimal.ONE)
                .and(new MaxGainBuyRule(analytics.getClosePrice(), Decimal.TEN));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(14);

        return strategy;
    }

    public Strategy createShortBuyEma540Strategy(CurrencyPair currencyPair, TimeFrame timeFrame) {
        ShortBuyAnalytics analytics = analyticsHelper.getShortBuyAnalytics(currencyPair, timeFrame);

        // Entry rule
        Rule entryRule = createBuyEntryRule(analytics)
                .and(new RisingUpIndicatorRule(analytics.getEma540())); // Rising trend

        // Exit rule
        Rule exitRule = new ModifiedStopGainRule(analytics.getClosePrice(), Decimal.ONE)
                .and(new MaxGainBuyRule(analytics.getClosePrice(), Decimal.valueOf(25)));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(540);

        return strategy;
    }

    public Strategy createShortBuyEma90Strategy1(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return createShortBuyEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(5)));
    }

    public Strategy createShortBuyEma90Strategy2(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return createShortBuyEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(10)));
    }

    public Strategy createShortBuyEma90Strategy3(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return createShortBuyEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(25)));
    }

    public Strategy createShortBuyEma90Strategy4(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return createShortBuyEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(50)));
    }

    public Strategy createShortBuyEma90Strategy5(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return createShortBuyEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(100)));
    }

    public Strategy createShortBuyEma90Strategy(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return createShortBuyEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.ONE));
    }

    private Strategy createShortBuyEma90(CurrencyPair currencyPair, TimeFrame timeFrame, StrategyRules strategyRules) {
        ShortBuyAnalytics analytics = analyticsHelper.getShortBuyAnalytics(currencyPair, timeFrame);

        // Entry rule
        Rule entryRule = createBuyEntryRule(analytics)
                .and(new RisingUpIndicatorRule(analytics.getEma90())); // Rising trend

        // Exit rule
        Rule exitRule = new ModifiedStopGainRule(analytics.getClosePrice(), strategyRules.getStopGain())
                .and(new MaxGainBuyRule(analytics.getClosePrice(), strategyRules.getMaxGainCorridor()));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(90);

        return strategy;
    }

    private Rule createBuyEntryRule(ShortBuyAnalytics analytics) {
        return new UnderIndicatorRule(analytics.getRsi(), Decimal.valueOf(20)) // RSI < 20
                .and(new UnderIndicatorRule(analytics.getStochK(), Decimal.valueOf(20))) // StochasticK < 20
                .and(new CrossedUpIndicatorRule(analytics.getStochK(), analytics.getStochD())); // K cross D from the bottom
    }

    public Strategy createShortSellEma90Strategy(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return createShortSellEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.ONE));
    }

    public Strategy createShortSellEma90Strategy1(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return createShortSellEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(5)));
    }

    public Strategy createShortSellEma90Strategy2(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return createShortSellEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(10)));
    }

    public Strategy createShortSellEma90Strategy3(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return createShortSellEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(25)));
    }

    public Strategy createShortSellEma90Strategy4(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return createShortSellEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(50)));
    }

    public Strategy createShortSellEma90Strategy5(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return createShortSellEma90(currencyPair, timeFrame, StrategyRules.of(Decimal.ONE, Decimal.valueOf(100)));
    }

    private Strategy createShortSellEma90(CurrencyPair currencyPair, TimeFrame timeFrame, StrategyRules strategyRules) {
        ShortBuyAnalytics analytics = analyticsHelper.getShortBuyAnalytics(currencyPair, timeFrame);

        // Entry rule
        Rule entryRule = createSellEntryRule(analytics)
                .and(new FallingDownIndicatorRule(analytics.getEma90()));

        // Exit rule
        Rule exitRule = new ModifiedStopGainRule(analytics.getClosePrice(), strategyRules.getStopGain())
                .and(new MaxGainSellRule(analytics.getClosePrice(), strategyRules.getMaxGainCorridor()));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(90);

        return strategy;
    }

    private Rule createSellEntryRule(ShortBuyAnalytics analytics) {
        return new OverIndicatorRule(analytics.getRsi(), Decimal.valueOf(80))
                .and(new OverIndicatorRule(analytics.getStochK(), Decimal.valueOf(80)))
                .and(new CrossedDownIndicatorRule(analytics.getStochK(), analytics.getStochD()));
    }

}
