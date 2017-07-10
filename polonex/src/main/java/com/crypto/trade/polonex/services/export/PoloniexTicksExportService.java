package com.crypto.trade.polonex.services.export;

import com.crypto.trade.polonex.dto.PoloniexTrade;
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
    @Scheduled(initialDelay = 60000, fixedDelay = 1000)
    public void exportData() {
        List<PoloniexTrade> poloniexTicks = tickersStorage.getTrades().getOrDefault("BTC_ETH", Collections.emptyList());
        StringBuilder sb = convert(poloniexTicks);
        csvFileWriter.write("poloniex_ticks", sb);
    }

    private StringBuilder convert(List<PoloniexTrade> poloniexTicks) {
        StringBuilder sb = new StringBuilder("time,timestamp,price,amount\n");

        for (PoloniexTrade poloniexTrade : poloniexTicks) {
            sb.append(poloniexTrade.getTradeTime().toLocalTime()).append(',')
                    .append(poloniexTrade.getTradeTime().toInstant().toEpochMilli() / 1000).append(',')
                    .append(poloniexTrade.getRate()).append(',')
                    .append(poloniexTrade.getTotal()).append(',')
                    .append('\n');
        }
        return sb;
    }
}
