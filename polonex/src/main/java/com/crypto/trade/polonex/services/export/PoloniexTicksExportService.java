package com.crypto.trade.polonex.services.export;

import com.crypto.trade.polonex.dto.PoloniexTick;
import com.crypto.trade.polonex.storage.TickersStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class PoloniexTicksExportService implements ExportDataService {

    @Autowired
    private CsvFileWriter csvFileWriter;
    @Autowired
    private TickersStorage tickersStorage;

    @Override
    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void exportData() {
        List<PoloniexTick> poloniexTicks = tickersStorage.getTicks().getOrDefault("BTC_ETH", Collections.emptyList());
        StringBuilder sb = convert(poloniexTicks);
        csvFileWriter.write("poloniex_ticks", sb);
    }

    private StringBuilder convert(List<PoloniexTick> poloniexTicks) {
        StringBuilder sb = new StringBuilder("time,timestamp,price,amount\n");

        for (PoloniexTick poloniexTick : poloniexTicks) {
            sb.append(poloniexTick.getTime().toLocalTime()).append(',')
                    .append(poloniexTick.getTime().toInstant().toEpochMilli() / 1000).append(',')
                    .append(poloniexTick.getLast()).append(',')
                    .append(poloniexTick.getQuoteVolume()).append(',')
                    .append('\n');
        }
        return sb;
    }
}
