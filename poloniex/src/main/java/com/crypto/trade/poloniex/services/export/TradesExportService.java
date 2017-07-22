package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.dto.PoloniexTrade;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.utils.CsvFileWriter;
import com.crypto.trade.poloniex.storage.TradesStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class TradesExportService implements ExportDataService {

    @Autowired
    private CsvFileWriter csvFileWriter;
    @Autowired
    private TradesStorage tradesStorage;

    @Override
    public void exportData() {
        List<PoloniexTrade> poloniexTrades = tradesStorage.getTrades().getOrDefault(CurrencyPair.BTC_ETH, Collections.emptyList());
        StringBuilder sb = convert(poloniexTrades);
        csvFileWriter.write("poloniex_trades", sb);
    }

    private StringBuilder convert(List<PoloniexTrade> poloniexTrades) {
        StringBuilder sb = new StringBuilder("time,timestamp,price,amount\n");

        for (PoloniexTrade poloniexTrade : poloniexTrades) {
            sb.append(poloniexTrade.getTradeTime().toLocalDateTime()).append(',')
                    .append(poloniexTrade.getTradeTime().toInstant().toEpochMilli() / 1000).append(',')
                    .append(poloniexTrade.getRate()).append(',')
                    .append(poloniexTrade.getAmount()).append(',')
                    .append('\n');
        }
        return sb;
    }

    @PreDestroy
    public void preDestroy() {
        exportData();
    }
}
