package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.AnalyticsService;
import com.crypto.trade.poloniex.services.analytics.StrategiesBuilder;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.storage.TickersStorage;
import eu.verdelhan.ta4j.*;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.EMAIndicator;
import eu.verdelhan.ta4j.indicators.trackers.RSIIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class AnalyticsExportService implements ExportDataService {

    @Autowired
    private CsvFileWriter csvFileWriter;
    @Autowired
    private TickersStorage tickersStorage;
    @Autowired
    private AnalyticsService analyticsService;
    @Autowired
    private StrategiesBuilder strategiesBuilder;

    @Override
    //@Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void exportData() {
        int period = 14;

        for (TimeFrame timeFrame : TimeFrame.values()) {
            TimeSeries ethSeries = tickersStorage.getCandles("BTC_ETH", timeFrame);
            ClosePriceIndicator closePrice = new ClosePriceIndicator(ethSeries);
            RSIIndicator rsi = new RSIIndicator(closePrice, period);
            StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(ethSeries, period);
            StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
            SMAIndicator sma = new SMAIndicator(stochK, period);
            EMAIndicator ema32 = new EMAIndicator(closePrice, 32);
            EMAIndicator ema128 = new EMAIndicator(closePrice, 128);
            Strategy strategy = strategiesBuilder.buildShortBuyStrategy(ethSeries);

            List<Indicator<?>> indicators = Arrays.asList(closePrice,
                    rsi,
                    stochK,
                    stochD,
                    sma,
                    ema32,
                    ema128);
            csvFileWriter.write("analytics(" + timeFrame.getDisplayName() + ")", convert(ethSeries, indicators, strategy));
        }
    }

    private StringBuilder convert(TimeSeries timeSeries, List<Indicator<?>> indicators, Strategy strategy) {
        StringBuilder sb = new StringBuilder("timestamp,a1,a2,close,rsi,stochK,stochD,sma,ema32,ema128\n");
        TradingRecord tradingRecord = new TradingRecord();
        final int nbTicks = timeSeries.getTickCount();
        for (int i = 0; i < nbTicks; i++) {
            Tick tick = timeSeries.getTick(i);
            sb.append(timeSeries.getTick(i).getEndTime().toLocalDateTime()).append(',');
            sb.append(analyticsService.analyzeTick(strategy, tick, i, tradingRecord)).append(',');
            for (Indicator<?> indicator : indicators) {
                sb.append(indicator.getValue(i)).append(',');
            }
            sb.append('\n');
        }

        sb.append('\n');
        sb.append("Trades: ").append(tradingRecord.getTradeCount());
        sb.append('\n');
        sb.append("Profit: ").append(new TotalProfitCriterion().calculate(timeSeries, tradingRecord));
        return sb;
    }
}
