package com.crypto.trade.poloniex.services.analytics;

import com.crypto.trade.poloniex.dto.PoloniexTrade;
import com.crypto.trade.poloniex.storage.TradesStorage;
import com.opencsv.CSVReader;
import eu.verdelhan.ta4j.*;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.RSIIndicator;
import eu.verdelhan.ta4j.trading.rules.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
public class StrategiesBuilder {

    public static final int DEFAULT_TIME_FRAME = 14;

    public static void main(String[] args) {
        StrategiesBuilder strategiesBuilder = new StrategiesBuilder();
        TradesStorage tradesStorage = new TradesStorage();
        // Reading all lines of the CSV file

        loadTicks(tradesStorage);

        TimeSeries oneMinuteSeries = null;//tradesStorage.getCandles(CurrencyPair.BTC_ETH, TimeFrame.ONE_MINUTE);
        Strategy shortBuyStrategy = strategiesBuilder.buildShortBuyStrategy(oneMinuteSeries, DEFAULT_TIME_FRAME);

        // Initializing the trading history
        TradingRecord tradingRecord = new TradingRecord();
        System.out.println("************************************************************");

        for (int i = 0; i < oneMinuteSeries.getTickCount(); i++) {
            int endIndex = i;
            Tick newTick = oneMinuteSeries.getTick(i);
            if (shortBuyStrategy.shouldEnter(endIndex)) {
                // Our strategy should enter
                log.debug("Strategy should ENTER on {}", endIndex);
                boolean entered = tradingRecord.enter(endIndex, newTick.getClosePrice(), Decimal.TEN);
                if (entered) {
                    Order entry = tradingRecord.getLastEntry();
                    log.debug("Entered on {} (price={}, amount={})", entry.getIndex(), entry.getPrice().toDouble(), entry.getAmount().toDouble());
                }
            } else if (shortBuyStrategy.shouldExit(endIndex, tradingRecord)) {
                // Our strategy should exit
                log.debug("Strategy should EXIT on {}", endIndex);
                boolean exited = tradingRecord.exit(endIndex, newTick.getClosePrice(), Decimal.TEN);
                if (exited) {
                    Order exit = tradingRecord.getLastExit();
                    log.debug("Exited on {} (price={}, amount={})", exit.getIndex(), exit.getPrice().toDouble(), exit.getAmount().toDouble());

                }
            }
        }

        // Running the strategy
        /*TradingRecord tradingRecord = oneMinuteSeries.run(shortBuyStrategy, Order.OrderType.BUY, Decimal.ONE);*/
        System.out.println("Number of trades for the strategy: " + tradingRecord.getTradeCount());

        // Analysis
        System.out.println("Total profit for the strategy: " + new TotalProfitCriterion().calculate(oneMinuteSeries, tradingRecord));
    }

    private static void loadTicks(TradesStorage tradesStorage) {
        InputStream stream = StrategiesBuilder.class.getClassLoader().getResourceAsStream("ticks/poloniex_ticks_2017-07-12.csv");
        CSVReader csvReader = null;
        List<String[]> lines = null;
        try {
            csvReader = new CSVReader(new InputStreamReader(stream, Charset.forName("UTF-8")), ',');
            lines = csvReader.readAll();
            lines.remove(0); // Removing header line
        } catch (IOException ioe) {
            Logger.getLogger(StrategiesBuilder.class.getName()).log(Level.SEVERE, "Unable to load trades from CSV", ioe);
        } finally {
            if (csvReader != null) {
                try {
                    csvReader.close();
                } catch (IOException ioe) {
                }
            }
        }

        if ((lines != null) && !lines.isEmpty()) {
            for (String[] tradeLine : lines) {
                ZonedDateTime time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(tradeLine[1]) * 1000), ZoneId.of("GMT+0"));
                PoloniexTrade trade = new PoloniexTrade(0L, time, tradeLine[2], "", "", "");
                tradesStorage.addTrade(CurrencyPair.BTC_ETH, trade);
            }
        }
    }

    /**
     * RSI 14, StochasticK 14, StochasticD 3
     * Buy on RSI < 20, K intersects D, K < 20
     * Sell +1%
     */
    public Strategy buildShortBuyStrategy(TimeSeries timeSeries, int timeFrame) {
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
     * RSI 14, RSI 5
     * Buy on RSI 5 intersects RSI 14 from bottom,
     * Sell on RSI 5 intersects RSI 14 from top,
     */
    public Strategy buildRsiSlowFastStrategy(TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsiSlow = new RSIIndicator(closePrice, 5);
        RSIIndicator rsiFast = new RSIIndicator(closePrice, 14);

        // Entry rule
        Rule entryRule = new UnderIndicatorRule(rsiSlow, Decimal.valueOf(70))
                .and(new CrossedUpIndicatorRule(rsiSlow, rsiFast));

        // Exit rule
        Rule exitRule = new CrossedDownIndicatorRule(rsiFast, rsiSlow);
        Strategy strategy = new Strategy(entryRule, exitRule);
        strategy.setUnstablePeriod(14);

        return strategy;
    }


    /**
     * Buys every first tick
     * Sells every second tick
     */
    public Strategy buildTestStrategy(TimeSeries timeSeries, int timeFrame) {
        // Entry rule
        Rule entryRule = new BooleanRule(true);

        // Exit rule
        Rule exitRule = new FixedRule(27, 34, 36, 50, 60);
        Strategy strategy = new Strategy(entryRule, exitRule);
        strategy.setUnstablePeriod(timeFrame);

        return strategy;
    }
}
