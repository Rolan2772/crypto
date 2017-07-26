package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.dto.PoloniexTrade;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.utils.CsvFileWriter;
import com.crypto.trade.poloniex.storage.TradesStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.SortedSet;

@Slf4j
@Service
public class TradesExportService implements ExportDataService<PoloniexTrade> {

    public static final String TRADES_FILE_NAME = "poloniex-trades-";
    public static final String STALE_TRADES_FILE_NAME = "poloniex-stale-trades-";

    @Autowired
    private CsvFileWriter csvFileWriter;
    @Autowired
    private TradesStorage tradesStorage;

    @Override
    public void exportData(CurrencyPair currencyPair, Collection<PoloniexTrade> data) {
        exportData(TRADES_FILE_NAME + currencyPair, data, false);
    }

    @Override
    public void exportData(String name, Collection<PoloniexTrade> trades, boolean append) {
        csvFileWriter.write(name, convert(trades), append);
    }

    private StringBuilder convert(Collection<PoloniexTrade> poloniexTrades) {
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
        SortedSet<PoloniexTrade> poloniexTrades = tradesStorage.getTrades(CurrencyPair.BTC_ETH);
        exportData(STALE_TRADES_FILE_NAME + CurrencyPair.BTC_ETH, poloniexTrades, true);
    }
}