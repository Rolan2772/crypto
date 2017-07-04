package com.crypto.trade.polonex.services.export;

import com.crypto.trade.polonex.storage.TickersStorage;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class CsvTickersExportDataService implements ExportDataService {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

    @Autowired
    private TickersStorage tickersStorage;

    @Scheduled(initialDelay = 60000, fixedDelay = 1000)

    public void exportData() {
        TimeSeries ethSeries = tickersStorage.generateMinuteCandles("BTC_ETH");

        StringBuilder sb = new StringBuilder("timePeriod,beginTime,endTime,openPrice,closePrice,maxPrice,minPrice,amount,volume\n");
        final int nbTicks = ethSeries.getTickCount();
        for (int i = 0; i < nbTicks; i++) {
            Tick tick = ethSeries.getTick(i);
            sb.append(tick.getTimePeriod()).append(',')
                    .append(tick.getBeginTime().toLocalDate()).append(',')
                    .append(tick.getEndTime().toLocalDate()).append(',')
                    .append(tick.getOpenPrice()).append(',')
                    .append(tick.getClosePrice()).append(',')
                    .append(tick.getMaxPrice()).append(',')
                    .append(tick.getMinPrice()).append(',')
                    .append(tick.getAmount()).append(',')
                    .append(tick.getVolume()).append(',')
                    .append('\n');
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("candles_" + LocalDateTime.now().format(formatter) + ".csv"));
            writer.write(sb.toString());
        } catch (IOException ioe) {
            log.error("Failed to write csv", ioe);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ioe) {
            }
        }
    }
}
