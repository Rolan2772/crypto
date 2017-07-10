package com.crypto.trade.polonex.services.export;

import com.crypto.trade.polonex.storage.TickersStorage;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.RSIIndicator;
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
        csvFileWriter.write("analytycs(1m)", convert(ethSeries1, closePrice1, rsi1));

        TimeSeries ethSeries5 = tickersStorage.generateCandles("BTC_ETH",
                5L,
                ChronoUnit.MINUTES,
                ChronoField.MINUTE_OF_HOUR);
        ClosePriceIndicator closePrice5 = new ClosePriceIndicator(ethSeries5);
        RSIIndicator rsi5 = new RSIIndicator(closePrice5, 14);
        csvFileWriter.write("analytycs(5m)", convert(ethSeries5, closePrice5, rsi5));

        TimeSeries ethSeries15 = tickersStorage.generateCandles("BTC_ETH",
                15L,
                ChronoUnit.MINUTES,
                ChronoField.MINUTE_OF_HOUR);
        ClosePriceIndicator closePrice15 = new ClosePriceIndicator(ethSeries15);
        RSIIndicator rsi15 = new RSIIndicator(closePrice15, 14);
        csvFileWriter.write("analytycs(15m)", convert(ethSeries15, closePrice15, rsi15));
    }

    private StringBuilder convert(TimeSeries timeSeries, ClosePriceIndicator closePriceIndicator, RSIIndicator rsiIndicator) {
        StringBuilder sb = new StringBuilder("timestamp,close,rsi\n");
        final int nbTicks = timeSeries.getTickCount();
        for (int i = 0; i < nbTicks; i++) {
            sb.append(timeSeries.getTick(i).getEndTime().toLocalDateTime()).append(',')
                    .append(closePriceIndicator.getValue(i)).append(',')
                    .append(rsiIndicator.getValue(i)).append(',')
                    .append('\n');
        }
        return sb;
    }
}
