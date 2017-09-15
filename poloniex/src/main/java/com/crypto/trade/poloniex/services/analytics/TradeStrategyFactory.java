package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.services.analytics.indicators.CachedDoubleEMAIndicator;
import com.crypto.trade.poloniex.services.analytics.indicators.CachedTripleEMAIndicator;
import com.crypto.trade.poloniex.services.analytics.rules.*;
import eu.verdelhan.ta4j.*;
import eu.verdelhan.ta4j.indicators.EMAIndicator;
import eu.verdelhan.ta4j.indicators.RSIIndicator;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import eu.verdelhan.ta4j.trading.rules.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class TradeStrategyFactory {

    public static final int DEFAULT_TIME_FRAME = 14;

    @Autowired
    private AnalyticsCache analyticsCache;
    @Autowired
    private IndicatorFactory indicatorFactory;

    /**
     * RSI 14, StochasticK 14, StochasticD 3
     * Buy on RSI < 20, K intersects D, K < 20
     * Sell +1%
     */
    public Strategy createShortBuyStrategy(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = analyticsCache.getIndicator(timeFrame,
                IndicatorType.RSI14,
                indicatorFactory.createRsi14Indicator(closePrice));
        StochasticOscillatorKIndicator stochK = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHK14,
                indicatorFactory.createStochK14(timeSeries));
        StochasticOscillatorDIndicator stochD = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHD3,
                indicatorFactory.createStochD3(stochK));

        // Entry rule
        Rule entryRule = new UnderIndicatorRule(rsi, Decimal.valueOf(20)) // RSI < 20
                .and(new UnderIndicatorRule(stochK, Decimal.valueOf(20))) // StochasticK < 20
                .and(new CrossedUpIndicatorRule(stochK, stochD)); // K cross D from the bottom

        // Exit rule
        Rule exitRule = new StopGainRule(closePrice, Decimal.valueOf(1));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(14);

        return strategy;
    }

    public Strategy createModifiedShortBuyStrategy1(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = analyticsCache.getIndicator(timeFrame,
                IndicatorType.RSI14,
                indicatorFactory.createRsi14Indicator(closePrice));
        StochasticOscillatorKIndicator stochK = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHK14,
                indicatorFactory.createStochK14(timeSeries));
        StochasticOscillatorDIndicator stochD = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHD3,
                indicatorFactory.createStochD3(stochK));

        // Entry rule
        Rule entryRule = new UnderIndicatorRule(rsi, Decimal.valueOf(20)) // RSI < 20
                .and(new UnderIndicatorRule(stochK, Decimal.valueOf(20))) // StochasticK < 20
                .and(new CrossedUpIndicatorRule(stochK, stochD)); // K cross D from the bottom

        // Exit rule
        Rule exitRule = new StopGainRule(closePrice, Decimal.valueOf(1))
                .and(new MaxGainRule(closePrice, Decimal.valueOf(0.5)));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(14);

        return strategy;
    }

    public Strategy createModifiedShortBuyStrategy2(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = analyticsCache.getIndicator(timeFrame,
                IndicatorType.RSI14,
                indicatorFactory.createRsi14Indicator(closePrice));
        StochasticOscillatorKIndicator stochK = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHK14,
                indicatorFactory.createStochK14(timeSeries));
        StochasticOscillatorDIndicator stochD = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHD3,
                indicatorFactory.createStochD3(stochK));

        // Entry rule
        Rule entryRule = new UnderIndicatorRule(rsi, Decimal.valueOf(20)) // RSI < 20
                .and(new UnderIndicatorRule(stochK, Decimal.valueOf(20))) // StochasticK < 20
                .and(new CrossedUpIndicatorRule(stochK, stochD)); // K cross D from the bottom

        // Exit rule
        Rule exitRule = new StopGainRule(closePrice, Decimal.valueOf(1))
                .and(new MaxGainRule(closePrice, Decimal.valueOf(0.25)));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(14);

        return strategy;
    }

    public Strategy createShortBuyEma90RisingTrendStrategy(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = analyticsCache.getIndicator(timeFrame,
                IndicatorType.RSI14,
                indicatorFactory.createRsi14Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        StochasticOscillatorKIndicator stochK = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHK14,
                indicatorFactory.createStochK14(timeSeries));
        StochasticOscillatorDIndicator stochD = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHD3,
                indicatorFactory.createStochD3(stochK));

        // Entry rule
        Rule entryRule = new UnderIndicatorRule(rsi, Decimal.valueOf(20)) // RSI < 20
                .and(new UnderIndicatorRule(stochK, Decimal.valueOf(20))) // StochasticK < 20
                .and(new CrossedUpIndicatorRule(stochK, stochD)) // K cross D from the bottom
                .and(new RisingUpIndicatorRule(ema90)); // Rising trend

        // Exit rule
        Rule exitRule = new StopGainRule(closePrice, Decimal.valueOf(1));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(90);

        return strategy;
    }

    public Strategy createModifiedShortBuyEma90RisingTrendStrategy1(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = analyticsCache.getIndicator(timeFrame,
                IndicatorType.RSI14,
                indicatorFactory.createRsi14Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        StochasticOscillatorKIndicator stochK = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHK14,
                indicatorFactory.createStochK14(timeSeries));
        StochasticOscillatorDIndicator stochD = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHD3,
                indicatorFactory.createStochD3(stochK));

        // Entry rule
        Rule entryRule = new UnderIndicatorRule(rsi, Decimal.valueOf(20)) // RSI < 20
                .and(new UnderIndicatorRule(stochK, Decimal.valueOf(20))) // StochasticK < 20
                .and(new CrossedUpIndicatorRule(stochK, stochD)) // K cross D from the bottom
                .and(new RisingUpIndicatorRule(ema90)); // Rising trend

        // Exit rule
        Rule exitRule = new StopGainRule(closePrice, Decimal.valueOf(1))
                .and(new MaxGainRule(closePrice, Decimal.valueOf(0.5)));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(90);

        return strategy;
    }

    // TODO: add ema 90 up on 1hour candles
    // EMA90 angle should be as big as possible || em60 should be distant from ema 90 1hour

    // One more Gaussian waves

    // one more Pivot indicator
    public Strategy createModifiedShortBuyEma90RisingTrendStrategy2(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = analyticsCache.getIndicator(timeFrame,
                IndicatorType.RSI14,
                indicatorFactory.createRsi14Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        StochasticOscillatorKIndicator stochK = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHK14,
                indicatorFactory.createStochK14(timeSeries));
        StochasticOscillatorDIndicator stochD = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHD3,
                indicatorFactory.createStochD3(stochK));

        // Entry rule
        Rule entryRule = new UnderIndicatorRule(rsi, Decimal.valueOf(20)) // RSI < 20
                .and(new UnderIndicatorRule(stochK, Decimal.valueOf(20))) // StochasticK < 20
                .and(new CrossedUpIndicatorRule(stochK, stochD)) // K cross D from the bottom
                .and(new RisingUpIndicatorRule(ema90)); // Rising trend

        // Exit rule
        Rule exitRule = new StopGainRule(closePrice, Decimal.valueOf(1))
                .and(new MaxGainRule(closePrice, Decimal.valueOf(0.25)));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(90);

        return strategy;
    }

    public Strategy createModifiedShortBuyEma540RisingTrendStrategy2(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = analyticsCache.getIndicator(timeFrame,
                IndicatorType.RSI14,
                indicatorFactory.createRsi14Indicator(closePrice));
        EMAIndicator ema540 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA540,
                indicatorFactory.createEma540Indicator(closePrice));
        StochasticOscillatorKIndicator stochK = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHK14,
                indicatorFactory.createStochK14(timeSeries));
        StochasticOscillatorDIndicator stochD = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHD3,
                indicatorFactory.createStochD3(stochK));

        // Entry rule
        Rule entryRule = new UnderIndicatorRule(rsi, Decimal.valueOf(20)) // RSI < 20
                .and(new UnderIndicatorRule(stochK, Decimal.valueOf(20))) // StochasticK < 20
                .and(new CrossedUpIndicatorRule(stochK, stochD)) // K cross D from the bottom
                .and(new RisingUpIndicatorRule(ema540)); // Rising trend

        // Exit rule
        Rule exitRule = new StopGainRule(closePrice, Decimal.valueOf(1))
                .and(new MaxGainRule(closePrice, Decimal.valueOf(0.25)));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(540);

        return strategy;
    }

    public Strategy createModifiedShortBuyEma90RisingTrendStrategy3(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = analyticsCache.getIndicator(timeFrame,
                IndicatorType.RSI14,
                indicatorFactory.createRsi14Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        StochasticOscillatorKIndicator stochK = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHK14,
                indicatorFactory.createStochK14(timeSeries));
        StochasticOscillatorDIndicator stochD = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHD3,
                indicatorFactory.createStochD3(stochK));

        // Entry rule
        Rule entryRule = new UnderIndicatorRule(rsi, Decimal.valueOf(20)) // RSI < 20
                .and(new UnderIndicatorRule(stochK, Decimal.valueOf(20))) // StochasticK < 20
                .and(new CrossedUpIndicatorRule(stochK, stochD)) // K cross D from the bottom
                .and(new RisingUpIndicatorRule(ema90)); // Rising trend

        // Exit rule
        Rule exitRule = new StopGainRule(closePrice, Decimal.valueOf(1))
                .and(new MaxGainRule(closePrice, Decimal.valueOf(10)));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(90);

        return strategy;
    }

    public Strategy createModifiedShortBuyEma90RisingTrendStrategy4(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = analyticsCache.getIndicator(timeFrame,
                IndicatorType.RSI14,
                indicatorFactory.createRsi14Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        StochasticOscillatorKIndicator stochK = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHK14,
                indicatorFactory.createStochK14(timeSeries));
        StochasticOscillatorDIndicator stochD = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHD3,
                indicatorFactory.createStochD3(stochK));

        // Entry rule
        Rule entryRule = new UnderIndicatorRule(rsi, Decimal.valueOf(20)) // RSI < 20
                .and(new UnderIndicatorRule(stochK, Decimal.valueOf(20))) // StochasticK < 20
                .and(new CrossedUpIndicatorRule(stochK, stochD)) // K cross D from the bottom
                .and(new RisingUpIndicatorRule(ema90)); // Rising trend

        // Exit rule
        Rule exitRule = new StopGainRule(closePrice, Decimal.valueOf(1))
                .and(new MaxGainRule(closePrice, Decimal.valueOf(20)));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(90);

        return strategy;
    }

    public Strategy createShortSellEma90FallingTrendStrategy1(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = analyticsCache.getIndicator(timeFrame,
                IndicatorType.RSI14,
                indicatorFactory.createRsi14Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        StochasticOscillatorKIndicator stochK = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHK14,
                indicatorFactory.createStochK14(timeSeries));
        StochasticOscillatorDIndicator stochD = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHD3,
                indicatorFactory.createStochD3(stochK));

        // Entry rule
        Rule entryRule = new OverIndicatorRule(rsi, Decimal.valueOf(80))
                .and(new OverIndicatorRule(stochK, Decimal.valueOf(80)))
                .and(new CrossedDownIndicatorRule(stochK, stochD))
                .and(new FallingDownIndicatorRule(ema90));

        // Exit rule
        Rule exitRule = new StopLossRule(closePrice, Decimal.valueOf(1))
                .and(new MaxLossRule(closePrice, Decimal.valueOf(1)));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(14);

        return strategy;
    }

    public Strategy createShortSellEma90FallingTrendStrategy2(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = analyticsCache.getIndicator(timeFrame,
                IndicatorType.RSI14,
                indicatorFactory.createRsi14Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        StochasticOscillatorKIndicator stochK = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHK14,
                indicatorFactory.createStochK14(timeSeries));
        StochasticOscillatorDIndicator stochD = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHD3,
                indicatorFactory.createStochD3(stochK));

        // Entry rule
        Rule entryRule = new OverIndicatorRule(rsi, Decimal.valueOf(80)) // RSI < 20
                .and(new OverIndicatorRule(stochK, Decimal.valueOf(80))) // StochasticK < 20
                .and(new CrossedDownIndicatorRule(stochK, stochD)) // K cross D from the bottom
                .and(new FallingDownIndicatorRule(ema90)); // Rising trend

        // Exit rule
        Rule exitRule = new StopLossRule(closePrice, Decimal.valueOf(0.51))
                .and(new MaxLossRule(closePrice, Decimal.valueOf(1)));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(14);

        return strategy;
    }

    public Strategy createShortSellEma90FallingTrendStrategy3(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = analyticsCache.getIndicator(timeFrame,
                IndicatorType.RSI14,
                indicatorFactory.createRsi14Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        StochasticOscillatorKIndicator stochK = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHK14,
                indicatorFactory.createStochK14(timeSeries));
        StochasticOscillatorDIndicator stochD = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHD3,
                indicatorFactory.createStochD3(stochK));

        // Entry rule
        Rule entryRule = new OverIndicatorRule(rsi, Decimal.valueOf(80)) // RSI < 20
                .and(new OverIndicatorRule(stochK, Decimal.valueOf(80))) // StochasticK < 20
                .and(new CrossedDownIndicatorRule(stochK, stochD)) // K cross D from the bottom
                .and(new FallingDownIndicatorRule(ema90)); // Rising trend

        // Exit rule
        Rule exitRule = new StopLossRule(closePrice, Decimal.valueOf(0.51))
                .and(new MaxLossRule(closePrice, Decimal.valueOf(2)));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(14);

        return strategy;
    }

    public Strategy createShortSellEma90FallingTrendStrategy4(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = analyticsCache.getIndicator(timeFrame,
                IndicatorType.RSI14,
                indicatorFactory.createRsi14Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        StochasticOscillatorKIndicator stochK = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHK14,
                indicatorFactory.createStochK14(timeSeries));
        StochasticOscillatorDIndicator stochD = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHD3,
                indicatorFactory.createStochD3(stochK));

        // Entry rule
        Rule entryRule = new OverIndicatorRule(rsi, Decimal.valueOf(80)) // RSI < 20
                .and(new OverIndicatorRule(stochK, Decimal.valueOf(80))) // StochasticK < 20
                .and(new CrossedDownIndicatorRule(stochK, stochD)) // K cross D from the bottom
                .and(new FallingDownIndicatorRule(ema90)); // Rising trend

        // Exit rule
        Rule exitRule = new StopLossRule(closePrice, Decimal.valueOf(0.51))
                .and(new MaxLossRule(closePrice, Decimal.valueOf(5)));
        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(14);

        return strategy;
    }

    public Strategy createRisingTrendStrategy(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        EMAIndicator ema5 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA5,
                indicatorFactory.createEma5Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        EMAIndicator ema100 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA100,
                indicatorFactory.createEma100Indicator(closePrice));

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
                .and(exit1).and(new StopGainRule(closePrice, Decimal.ONE));

        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(100);

        return strategy;
    }

    public Strategy createFallingTrendStrategy(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        EMAIndicator ema5 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA5,
                indicatorFactory.createEma5Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        EMAIndicator ema100 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA100,
                indicatorFactory.createEma100Indicator(closePrice));

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

        Rule entryRule = entry1.or(entry2).and(new StopLossRule(closePrice, Decimal.ONE));

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

        Strategy strategy = new BaseStrategy(exitRule, entryRule);
        strategy.setUnstablePeriod(100);

        return strategy;
    }

    public Strategy createRisingTripleEmaStrategyCorrected(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);

        EMAIndicator ema5 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA5,
                indicatorFactory.createEma5Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        EMAIndicator emaEma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA_EMA90,
                indicatorFactory.createEmaEma90Indicator(ema90));
        CachedDoubleEMAIndicator dma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.DMA90,
                indicatorFactory.createDma90Indicator(closePrice, ema90, emaEma90));
        EMAIndicator emaEmaEma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA_EMA_EMA90,
                indicatorFactory.createEmaEmaEma90Indicator(emaEma90));
        CachedTripleEMAIndicator tma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.TMA90,
                indicatorFactory.createTma90Indicator(closePrice, ema90, emaEma90, emaEmaEma90));

        Rule entryRule = new LowerRule(tma90, dma90)
                .and(new LowerRule(tma90, ema90))
                .and(new CrossedUpIndicatorRule(ema5, tma90));

        Rule exitRule = new UpperRule(tma90, ema90)
                .and(new CrossedDownIndicatorRule(ema5, tma90))
                .and(new StopGainRule(closePrice, Decimal.ONE));

        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(270);

        return strategy;
    }

    public Strategy createFallingTripleEmaStrategy(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);

        EMAIndicator ema5 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA5,
                indicatorFactory.createEma5Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        EMAIndicator emaEma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA_EMA90,
                indicatorFactory.createEmaEma90Indicator(ema90));
        CachedDoubleEMAIndicator dma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.DMA90,
                indicatorFactory.createDma90Indicator(closePrice, ema90, emaEma90));
        EMAIndicator emaEmaEma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA_EMA_EMA90,
                indicatorFactory.createEmaEmaEma90Indicator(emaEma90));
        CachedTripleEMAIndicator tma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.TMA90,
                indicatorFactory.createTma90Indicator(closePrice, ema90, emaEma90, emaEmaEma90));

        Rule exitRule = new UpperRule(tma90, ema90)
                .and(new CrossedUpIndicatorRule(ema5, tma90))
                .and(new StopLossRule(closePrice, Decimal.ONE));

        Rule entryRule = new LowerRule(tma90, dma90)
                .and(new LowerRule(tma90, ema90))
                .and(new CrossedDownIndicatorRule(ema5, tma90));

        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(270);

        return strategy;
    }

    public Strategy createRisingTripleEmaStrategy2(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);

        EMAIndicator ema5 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA5,
                indicatorFactory.createEma5Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        EMAIndicator emaEma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA_EMA90,
                indicatorFactory.createEmaEma90Indicator(ema90));
        CachedDoubleEMAIndicator dma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.DMA90,
                indicatorFactory.createDma90Indicator(closePrice, ema90, emaEma90));
        EMAIndicator emaEmaEma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA_EMA_EMA90,
                indicatorFactory.createEmaEmaEma90Indicator(emaEma90));
        CachedTripleEMAIndicator tma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.TMA90,
                indicatorFactory.createTma90Indicator(closePrice, ema90, emaEma90, emaEmaEma90));

        Rule entryRule = new UpperRule(tma90, ema90)
                .and(new CrossedUpIndicatorRule(ema5, tma90));

        Rule exitRule = new LowerRule(tma90, dma90)
                .and(new LowerRule(tma90, ema90))
                .and(new CrossedDownIndicatorRule(ema5, tma90))
                .and(new StopGainRule(closePrice, Decimal.ONE))
                .and(new MaxGainRule(closePrice, Decimal.valueOf(1)));

        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(270);

        return strategy;
    }

    public Strategy createModifiedRisingTrendStrategy(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);

        EMAIndicator ema5 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA5,
                indicatorFactory.createEma5Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        EMAIndicator ema100 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA100,
                indicatorFactory.createEma100Indicator(closePrice));

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
                .and(exit1)
                .and(new StopGainRule(closePrice, Decimal.valueOf(40)));

        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(100);

        return strategy;
    }
}
