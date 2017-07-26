package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.utils.CsvFileWriter;
import com.crypto.trade.poloniex.storage.CandlesStorage;
import com.crypto.trade.poloniex.storage.PoloniexTradingRecord;
import com.crypto.trade.poloniex.storage.TimeFrameStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class OrdersExportService implements ExportDataService<TimeFrameStorage> {

    public static final String ORDERS_FILE_NAME = "orders-";

    @Autowired
    private CsvFileWriter csvFileWriter;
    @Autowired
    private CandlesStorage candlesStorage;
    @Autowired
    private ExportHelper exportHelper;

    @Override
    public void exportData(CurrencyPair currencyPair, Collection<TimeFrameStorage> data) {
        exportData(ORDERS_FILE_NAME + currencyPair, data, false);
    }

    @Override
    public void exportData(String name, Collection<TimeFrameStorage> data, boolean append) {
        for (TimeFrameStorage timeFrameStorage : data) {
            TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
            List<PoloniexTradingRecord> tradingRecords = timeFrameStorage.getAllTradingRecords();

            StringBuilder sb = new StringBuilder("name,id,time,index,price,amount,type\n");
            tradingRecords.forEach(tradingRecord -> {
                String trName = tradingRecord.getStrategyName() + "-tr-" + tradingRecord.getId();
                tradingRecord.getOrders().forEach(poloniexOrder -> sb.append(exportHelper.convertOrder(trName, poloniexOrder)).append("\n"));
            });

            csvFileWriter.write(name + "(" + timeFrame.getDisplayName() + ")", sb, append);
        }
    }

    @PreDestroy
    public void preDestroy() {
        List<TimeFrameStorage> btcEth = candlesStorage.getData(CurrencyPair.BTC_ETH);
        exportData(CurrencyPair.BTC_ETH, btcEth);
    }
}
