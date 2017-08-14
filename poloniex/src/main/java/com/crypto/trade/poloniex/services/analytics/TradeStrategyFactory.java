package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.services.analytics.rules.FallingDownIndicatorRule;
import com.crypto.trade.poloniex.services.analytics.rules.LowerRule;
import com.crypto.trade.poloniex.services.analytics.rules.RisingUpIndicatorRule;
import com.crypto.trade.poloniex.services.analytics.rules.UpperRule;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.EMAIndicator;
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

    public Strategy createShortBuyEma90RisingTrendStrategy(TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        EMAIndicator ema90 = new EMAIndicator(closePrice, 90);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(timeSeries, 14);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);

        // Entry rule
        Rule entryRule = new UnderIndicatorRule(rsi, Decimal.valueOf(20)) // RSI < 20
                .and(new UnderIndicatorRule(stochK, Decimal.valueOf(20))) // StochasticK < 20
                .and(new CrossedUpIndicatorRule(stochK, stochD)) // K cross D from the bottom
                .and(new RisingUpIndicatorRule(ema90)); // Rising trend

        // Exit rule
        Rule exitRule = new StopGainRule(closePrice, Decimal.valueOf(1));
        Strategy strategy = new Strategy(entryRule, exitRule);
        strategy.setUnstablePeriod(90);

        return strategy;
    }

    public Strategy createShortBuyEma90NotFallingTrendStrategy(TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        EMAIndicator ema90 = new EMAIndicator(closePrice, 90);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(timeSeries, 14);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);

        // Entry rule
        Rule entryRule = new UnderIndicatorRule(rsi, Decimal.valueOf(20)) // RSI < 20
                .and(new UnderIndicatorRule(stochK, Decimal.valueOf(20))) // StochasticK < 20
                .and(new CrossedUpIndicatorRule(stochK, stochD)) // K cross D from the bottom
                .and(new NotRule(new FallingDownIndicatorRule(ema90))); // Rising trend

        // Exit rule
        Rule exitRule = new StopGainRule(closePrice, Decimal.valueOf(1));
        Strategy strategy = new Strategy(entryRule, exitRule);
        strategy.setUnstablePeriod(90);

        return strategy;
    }

    public Strategy createRisingTrendStrategy(TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        EMAIndicator ema5 = new EMAIndicator(closePrice, 5);
        EMAIndicator ema90 = new EMAIndicator(closePrice, 90);
        EMAIndicator ema100 = new EMAIndicator(closePrice, 100);

        // ema90[0] >= ema90[-1] and ma05 > ma100
        Rule trendUp = new NotRule(new FallingDownIndicatorRule(ema90))
                .and(new UpperRule(ema5, ema100));
        // ema90[-1] >= ema90[-2] and ema05[-1] < ma100[-1]
        Rule buySignal1 = new NotRule(new FallingDownIndicatorRule(ema90, 1))
                .and(new LowerRule(ema5, ema100, 1));
        // ema90[-1] <= ema90[-2] and ema05[-1] < ema100[-1]
        Rule buySignal2 = new NotRule(new RisingUpIndicatorRule(ema90, 1))
                .and(new LowerRule(ema5, ema100, 1));

        Rule entry1 = trendUp.and(buySignal1);
        Rule entry2 = trendUp.and(new NotRule(buySignal1)).and(buySignal2);

        Rule entryRule = entry1.or(entry2);

        // Exit rule
        // ema90[0] <= ema90[-1] and ema05 < ema100
        Rule trendDown = new NotRule(new RisingUpIndicatorRule(ema90))
                .and(new LowerRule(ema5, ema100));
        // ema90[0] < ema90[-1] and ema05 > ema100
        Rule trendPreDown = new FallingDownIndicatorRule(ema90)
                .and(new UpperRule(ema5, ema100));
        // ema90[-1] >= ema90[-2] and ema05[-1] > ema100[-1]
        Rule exit1 = new NotRule(new FallingDownIndicatorRule(ema90, 1))
                .and(new UpperRule(ema5, ema100, 1));

        Rule exitRule = new OrRule(trendDown, trendPreDown)
                .and(exit1);

        Strategy strategy = new Strategy(entryRule, exitRule);
        strategy.setUnstablePeriod(100);

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
