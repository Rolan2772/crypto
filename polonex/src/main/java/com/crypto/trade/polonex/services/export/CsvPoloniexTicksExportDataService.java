package com.crypto.trade.polonex.services.export;

import com.crypto.trade.polonex.dto.PolonexTick;
import com.crypto.trade.polonex.storage.TickersStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class CsvPoloniexTicksExportDataService implements ExportDataService {


    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private TickersStorage tickersStorage;

    @Scheduled(initialDelay = 60000, fixedDelay = 5000)
    public void exportData() {
        List<PolonexTick> polonexTicks = tickersStorage.getTickers().getOrDefault("BTC_ETH", Collections.emptyList());

        StringBuilder sb = new StringBuilder("timestamp,price,amount\n");
        for (PolonexTick polonexTick : polonexTicks) {
            sb.append(polonexTick.getTime().toInstant().toEpochMilli() / 1000).append(',')
                    .append(polonexTick.getLast()).append(',')
                    .append(polonexTick.getQuoteVolume()).append(',')
                    .append('\n');
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("poloniexTicks_" + LocalDateTime.now().format(formatter) + ".csv"));
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
