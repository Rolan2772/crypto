package com.crypto.trade.polonex.services.export;

import com.crypto.trade.polonex.services.analytics.TimeFrame;
import com.crypto.trade.polonex.storage.TickersStorage;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.EMAIndicator;
import eu.verdelhan.ta4j.indicators.trackers.RSIIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SmoothedRSIIndicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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

    @Override
    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void exportData() {
        int period = 14;

        for (TimeFrame timeFrame : TimeFrame.values()) {
            TimeSeries ethSeries = tickersStorage.generateCandles("BTC_ETH", timeFrame);
            ClosePriceIndicator closePrice = new ClosePriceIndicator(ethSeries);
            RSIIndicator rsi = new RSIIndicator(closePrice, period);
            StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(ethSeries, period);
            StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
            SMAIndicator sma = new SMAIndicator(stochK, period);
            EMAIndicator ema32 = new EMAIndicator(closePrice, 32);
            EMAIndicator ema128 = new EMAIndicator(closePrice, 128);
            SmoothedRSIIndicator smoothedRSIIndicator = new SmoothedRSIIndicator(closePrice, period);

            List<Indicator<?>> indicators = Arrays.asList(closePrice,
                    rsi,
                    stochK,
                    stochD,
                    sma,
                    ema32,
                    ema128,
                    smoothedRSIIndicator);
            csvFileWriter.write("analytics(" + timeFrame.getDisplayName() + ")", convert(ethSeries, indicators));
        }
    }

    private StringBuilder convert(TimeSeries timeSeries, List<Indicator<?>> indicators) {
        StringBuilder sb = new StringBuilder("timestamp,close,rsi,stochK,stochD,sma,ema32,ema128,smoothedRsi\n");
        final int nbTicks = timeSeries.getTickCount();
        for (int i = 0; i < nbTicks; i++) {
            sb.append(timeSeries.getTick(i).getEndTime().toLocalDateTime()).append(',');
            for (Indicator<?> indicator : indicators) {
                sb.append(indicator.getValue(i)).append(',');
            }
            sb.append('\n');
        }
        return sb;
    }
}
