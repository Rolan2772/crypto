package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.AnalyticsService;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.StrategiesBuilder;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.utils.CsvFileWriter;
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

import javax.annotation.PreDestroy;
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
    private AnalyticsService historyAnalyticsService;
    @Autowired
    private StrategiesBuilder strategiesBuilder;

    @Override
    public void exportData() {
        int indicatorTimeFrame = StrategiesBuilder.DEFAULT_TIME_FRAME;

        for (TimeFrame timeFrame : TimeFrame.values()) {
            TimeSeries ethSeries = tickersStorage.getCandles(CurrencyPair.BTC_ETH, timeFrame);
            ClosePriceIndicator closePrice = new ClosePriceIndicator(ethSeries);
            RSIIndicator rsi = new RSIIndicator(closePrice, indicatorTimeFrame);
            StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(ethSeries, indicatorTimeFrame);
            StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
            SMAIndicator sma = new SMAIndicator(stochK, indicatorTimeFrame);
            EMAIndicator ema32 = new EMAIndicator(closePrice, 32);
            EMAIndicator ema128 = new EMAIndicator(closePrice, 128);
            Strategy strategy = strategiesBuilder.buildShortBuyStrategy(ethSeries, indicatorTimeFrame);

            List<Indicator<?>> indicators = Arrays.asList(closePrice,
                    rsi,
                    stochK,
                    stochD,
                    sma,
                    ema32,
                    ema128);

            StringBuilder sb = convert(ethSeries, indicators, strategy);

            TradingRecord real = tickersStorage.getTradingRecords().get(timeFrame);
            sb.append('\n');
            sb.append("Real trades: ");
            sb.append("Trades: ").append(real.getTradeCount());
            sb.append('\n');
            sb.append("Profit: ").append(new TotalProfitCriterion().calculate(ethSeries, real));
            sb.append('\n');

            csvFileWriter.write("analytics(" + timeFrame.getDisplayName() + ")", sb);
        }
    }

    private StringBuilder convert(TimeSeries timeSeries, List<Indicator<?>> indicators, Strategy strategy) {
        StringBuilder sb = new StringBuilder("timestamp,action,close,rsi,stochK,stochD,sma,ema32,ema128\n");
        TradingRecord tradingRecord = new TradingRecord();
        final int nbTicks = timeSeries.getTickCount();
        for (int i = 0; i < nbTicks; i++) {
            Tick tick = timeSeries.getTick(i);
            sb.append(timeSeries.getTick(i).getEndTime().toLocalDateTime()).append(',');
            sb.append(historyAnalyticsService.analyzeTick(strategy, tick, i, tradingRecord)).append(',');
            for (Indicator<?> indicator : indicators) {
                sb.append(indicator.getValue(i)).append(',');
            }
            sb.append('\n');
        }

        sb.append('\n');
        sb.append("Analytics: ");
        sb.append("Trades: ").append(tradingRecord.getTradeCount());
        sb.append('\n');
        sb.append("Profit: ").append(new TotalProfitCriterion().calculate(timeSeries, tradingRecord));
        return sb;
    }

    @PreDestroy
    public void preDestroy() {
        exportData();
    }
}
