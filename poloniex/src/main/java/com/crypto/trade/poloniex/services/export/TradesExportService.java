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
public class TradesExportService implements MemoryExportService<PoloniexTrade>, ExcessDataExportService<PoloniexTrade> {

    public static final String TRADES_FILE_NAME = "poloniex-trades-";
    public static final String STALE_TRADES_FILE_NAME = "poloniex-stale-trades-";

    @Autowired
    private CsvFileWriter csvFileWriter;
    @Autowired
    private TradesStorage tradesStorage;

    @Override
    public void exportMemoryData(CurrencyPair currencyPair, Collection<PoloniexTrade> trades, OsType osType) {
        csvFileWriter.write(new ExportData(currencyPair, osType + "-" + TRADES_FILE_NAME + currencyPair, convert(trades)));
    }

    @Override
    public void exportMemoryData(CurrencyPair currencyPair, Collection<PoloniexTrade> trades) {
        csvFileWriter.write(new ExportData(currencyPair, TRADES_FILE_NAME + currencyPair, convert(trades)));
    }

    @Override
    public void exportExcessData(String name, Collection<PoloniexTrade> trades) {
        csvFileWriter.write(new ExportData(CurrencyPair.BTC_ETH, name, convert(trades)), true);
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
        //exportExcessData(STALE_TRADES_FILE_NAME + CurrencyPair.BTC_ETH, poloniexTrades);
    }
}