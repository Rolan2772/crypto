package com.crypto.trade.poloniex.services.analytics;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.RSIIndicator;
import eu.verdelhan.ta4j.trading.rules.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TradeStrategyFactory {

    public static final int DEFAULT_TIME_FRAME = 14;

    /**
     * RSI 14, StochasticK 14, StochasticD 3
     * Buy on RSI < 20, K intersects D, K < 20
     * Sell +1%
     */
    public Strategy createShortBuyStrategy(TimeSeries timeSeries, int timeFrame) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = new RSIIndicator(closePrice, timeFrame);
        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(timeSeries, timeFrame);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);

        // Entry rule
        Rule entryRule = new UnderIndicatorRule(rsi, Decimal.valueOf(20)) // RSI < 20
                .and(new UnderIndicatorRule(stochK, Decimal.valueOf(20))) // StochasticK < 20
                .and(new CrossedUpIndicatorRule(stochK, stochD)); // K cross D from the bottom

        // Exit rule
        Rule exitRule = new StopGainRule(closePrice, Decimal.valueOf(1));
        Strategy strategy = new Strategy(entryRule, exitRule);
        strategy.setUnstablePeriod(timeFrame);

        return strategy;
    }

    /**
     * Buys on every candle
     * Sells on candles: 27, 34, 36, 50, 60
     */
    public Strategy createTestStrategy(TimeSeries timeSeries, int timeFrame) {
        // Entry rule
        Rule entryRule = new BooleanRule(true);

        // Exit rule
        Rule exitRule = new FixedRule(27, 34, 36, 50, 60);
        Strategy strategy = new Strategy(entryRule, exitRule);
        strategy.setUnstablePeriod(timeFrame);

        return strategy;
    }
}
