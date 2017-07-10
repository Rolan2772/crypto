package com.crypto.trade.polonex.services.export;

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

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

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
        TimeSeries ethSeries1 = tickersStorage.generateCandles("BTC_ETH",
                1L,
                ChronoUnit.MINUTES,
                ChronoField.MINUTE_OF_HOUR);
        ClosePriceIndicator closePrice1 = new ClosePriceIndicator(ethSeries1);
        RSIIndicator rsi1 = new RSIIndicator(closePrice1, 14);
        StochasticOscillatorKIndicator stochK1 = new StochasticOscillatorKIndicator(ethSeries1, 14);
        StochasticOscillatorDIndicator stochD1 = new StochasticOscillatorDIndicator(stochK1);
        SMAIndicator sma1 = new SMAIndicator(stochK1, 3);

        csvFileWriter.write("analytycs(1m)", convert(ethSeries1, closePrice1, rsi1, stochK1, stochD1, sma1));

        TimeSeries ethSeries5 = tickersStorage.generateCandles("BTC_ETH",
                5L,
                ChronoUnit.MINUTES,
                ChronoField.MINUTE_OF_HOUR);
        ClosePriceIndicator closePrice5 = new ClosePriceIndicator(ethSeries5);
        RSIIndicator rsi5 = new RSIIndicator(closePrice5, 14);
        StochasticOscillatorKIndicator stochK5 = new StochasticOscillatorKIndicator(ethSeries5, 14);
        StochasticOscillatorDIndicator stochD5 = new StochasticOscillatorDIndicator(stochK5);
        SMAIndicator sma5 = new SMAIndicator(stochK5, 3);
        csvFileWriter.write("analytycs(5m)", convert(ethSeries5, closePrice5, rsi5, stochK5, stochD5, sma5));


        TimeSeries ethSeries15 = tickersStorage.generateCandles("BTC_ETH",
                15L,
                ChronoUnit.MINUTES,
                ChronoField.MINUTE_OF_HOUR);
        ClosePriceIndicator closePrice15 = new ClosePriceIndicator(ethSeries15);
        RSIIndicator rsi15 = new RSIIndicator(closePrice15, 14);
        StochasticOscillatorKIndicator stochK15 = new StochasticOscillatorKIndicator(ethSeries15, 14);
        StochasticOscillatorDIndicator stochD15 = new StochasticOscillatorDIndicator(stochK15);
        SMAIndicator sma15 = new SMAIndicator(stochK15, 3);
        csvFileWriter.write("analytycs(15m)", convert(ethSeries15, closePrice15, rsi15, stochK15, stochD15, sma15));
    }

    private StringBuilder convert(TimeSeries timeSeries, ClosePriceIndicator closePriceIndicator, RSIIndicator rsiIndicator, StochasticOscillatorKIndicator stockK, StochasticOscillatorDIndicator stochD, SMAIndicator sma) {
        StringBuilder sb = new StringBuilder("timestamp,close,rsi,stochK,stockD,sma\n");
        final int nbTicks = timeSeries.getTickCount();
        for (int i = 0; i < nbTicks; i++) {
            sb.append(timeSeries.getTick(i).getEndTime().toLocalDateTime()).append(',')
                    .append(closePriceIndicator.getValue(i)).append(',')
                    .append(rsiIndicator.getValue(i)).append(',')
                    .append(stockK.getValue(i)).append(',')
                    .append(stochD.getValue(i)).append(',')
                    .append(sma.getValue(i)).append(',')
                    .append('\n');
        }
        return sb;
    }
}
