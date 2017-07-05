package com.crypto.trade.polonex.services.export;

import com.crypto.trade.polonex.storage.TickersStorage;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.RSIIndicator;
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
public class CsvExportDataService implements ExportDataService {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private TickersStorage tickersStorage;

    @Scheduled(initialDelay = 60000, fixedDelay = 5000)
    public void exportData() {
        TimeSeries ethSeries = tickersStorage.generateMinuteCandles("BTC_ETH");
        ClosePriceIndicator closePrice = new ClosePriceIndicator(ethSeries);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);

        StringBuilder sb = new StringBuilder("timestamp,close,rsi\n");
        final int nbTicks = ethSeries.getTickCount();
        for (int i = 0; i < nbTicks; i++) {
            sb.append(ethSeries.getTick(i).getEndTime().toLocalDateTime()).append(',')
                    .append(closePrice.getValue(i)).append(',')
                                        /*.append(typicalPrice.getValue(i)).append(',')
                                        .append(priceVariation.getValue(i)).append(',')
                                        .append(shortSma.getValue(i)).append(',')
                                        .append(longSma.getValue(i)).append(',')
                                        .append(shortEma.getValue(i)).append(',')
                                        .append(longEma.getValue(i)).append(',')
                                        .append(ppo.getValue(i)).append(',')
                                        .append(roc.getValue(i)).append(',')*/
                    .append(rsi.getValue(i)).append(',')
                                        /*.append(williamsR.getValue(i)).append(',')
                                        .append(atr.getValue(i)).append(',')
                                        .append(sd.getValue(i))*/.append('\n');
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("eth_" + LocalDateTime.now().format(formatter) + ".csv"));
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
