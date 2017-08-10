package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.services.analytics.rules.RisingUpIndicatorRule;
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

    public Strategy createRisingTrendStrategy(TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);

        EMAIndicator ema5 = new EMAIndicator(closePrice, 5);
        EMAIndicator ema90 = new EMAIndicator(closePrice, 90);
        EMAIndicator ema100 = new EMAIndicator(closePrice, 100);

        // Entry rule
        // (A & B) & (C & D) & (C & D)
        // ma90 rising or not moving (last two points) and ma05 > ma100 (last two points)
        // or
        // (A & B) & !(C & D) & (E & C)
        // ma90[last] >= ma90[last - 1] and ma05>ma100 and !(ma90[1] >= ma90[2] and ma05[1]<ma100[1]) and (ma90[1] <= ma90[2] and ma05[1]<ma100[1])
        Rule entryRule = new CrossedUpIndicatorRule(ema5, ema90)
                .and(new RisingUpIndicatorRule(ema90));

        // Exit rule
        // ((A & B) | (C & D)) & (E & F)
        // ((change(ma90)<=0 and ma05<ma100) or (change(ma90)<0 and ma05>ma100)) and (ma90[1] >= ma90[2] and ma05[1]>ma100[1])
        Rule exitRule = new CrossedDownIndicatorRule(ema5, ema90);
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
