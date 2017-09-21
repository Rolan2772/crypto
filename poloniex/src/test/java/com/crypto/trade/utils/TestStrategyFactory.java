package com.crypto.trade.utils;

import eu.verdelhan.ta4j.BaseStrategy;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.trading.rules.BooleanRule;

public class TestStrategyFactory {

    public static Strategy createEntryStrategy() {
        return createBooleanStrategy(true, false);
    }

    public static Strategy createExitStrategy() {
        return createBooleanStrategy(false, true);
    }

    public static Strategy createNonTradingStrategy() {
        return createBooleanStrategy(false, false);
    }

    public static Strategy createAlwaysTradingStrategy() {
        return createBooleanStrategy(true, true);
    }

    private static Strategy createBooleanStrategy(boolean entryRule, boolean exitRule) {
        Rule entry = new BooleanRule(entryRule);
        Rule exit = new BooleanRule(exitRule);

        return new BaseStrategy(entry, exit);
    }
}
