package com.crypto.trade.polonex.services.export;

import com.crypto.trade.polonex.services.analytics.TimeFrame;
import com.crypto.trade.polonex.storage.TickersStorage;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.RSIIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

        for (TimeFrame timeFrame : TimeFrame.values()) {
            TimeSeries ethSeries = tickersStorage.generateCandles("BTC_ETH", timeFrame);
            ClosePriceIndicator closePrice1 = new ClosePriceIndicator(ethSeries);
            RSIIndicator rsi = new RSIIndicator(closePrice1, 14);
            StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(ethSeries, 14);
            StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
            SMAIndicator sma = new SMAIndicator(stochK, 14);
            csvFileWriter.write("analytics(" + timeFrame.getDisplayName() + ")", convert(ethSeries, closePrice1, rsi, stochK, stochD, sma));
        }
    }

    private StringBuilder convert(TimeSeries timeSeries, ClosePriceIndicator closePriceIndicator, RSIIndicator rsiIndicator, StochasticOscillatorKIndicator stochK, StochasticOscillatorDIndicator stochD, SMAIndicator sma) {
        StringBuilder sb = new StringBuilder("timestamp,close,rsi,stochK,stochD,sma\n");
        final int nbTicks = timeSeries.getTickCount();
        for (int i = 0; i < nbTicks; i++) {
            sb.append(timeSeries.getTick(i).getEndTime().toLocalDateTime()).append(',')
                    .append(closePriceIndicator.getValue(i)).append(',')
                    .append(rsiIndicator.getValue(i)).append(',')
                    .append(stochK.getValue(i)).append(',')
                    .append(stochD.getValue(i)).append(',')
                    .append(sma.getValue(i)).append(',')
                    .append('\n');
        }
        return sb;
    }
}
