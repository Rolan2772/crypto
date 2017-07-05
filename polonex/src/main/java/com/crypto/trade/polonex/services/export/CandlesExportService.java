package com.crypto.trade.polonex.services.export;

import com.crypto.trade.polonex.storage.TickersStorage;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class CandlesExportService implements ExportDataService {

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
        csvFileWriter.write("candles(1m)", convert(ethSeries1));

        TimeSeries ethSeries5 = tickersStorage.generateCandles("BTC_ETH",
                5L,
                ChronoUnit.MINUTES,
                ChronoField.MINUTE_OF_HOUR);
        csvFileWriter.write("candles(5m)", convert(ethSeries5));

        TimeSeries ethSeries15 = tickersStorage.generateCandles("BTC_ETH",
                15L,
                ChronoUnit.MINUTES,
                ChronoField.MINUTE_OF_HOUR);
        csvFileWriter.write("candles(15m)", convert(ethSeries15));
    }

    private StringBuilder convert(TimeSeries timeSeries) {
        StringBuilder sb = new StringBuilder("timePeriod,beginTime,endTime,openPrice,closePrice,maxPrice,minPrice,amount,volume\n");
        final int nbTicks = timeSeries.getTickCount();
        for (int i = 0; i < nbTicks; i++) {
            Tick tick = timeSeries.getTick(i);
            sb.append(tick.getTimePeriod()).append(',')
                    .append(tick.getBeginTime().toLocalTime()).append(',')
                    .append(tick.getEndTime().toLocalTime()).append(',')
                    .append(tick.getOpenPrice()).append(',')
                    .append(tick.getClosePrice()).append(',')
                    .append(tick.getMaxPrice()).append(',')
                    .append(tick.getMinPrice()).append(',')
                    .append(tick.getAmount()).append(',')
                    .append(tick.getVolume()).append(',')
                    .append('\n');
        }
        return sb;
    }
}
