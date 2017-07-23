package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.StrategiesBuilder;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.utils.CsvFileWriter;
import com.crypto.trade.poloniex.storage.CandlesStorage;
import com.crypto.trade.poloniex.storage.PoloniexTradingRecord;
import com.crypto.trade.poloniex.storage.TimeFrameStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class OrdersExportService implements ExportDataService {

    @Autowired
    private CsvFileWriter csvFileWriter;
    @Autowired
    private CandlesStorage candlesStorage;
    @Autowired
    private ExportHelper exportHelper;

    @Override
    public void exportData() {
        int indicatorTimeFrame = StrategiesBuilder.DEFAULT_TIME_FRAME;

        List<TimeFrameStorage> btcEth = candlesStorage.getCandles().getOrDefault(CurrencyPair.BTC_ETH, Collections.emptyList());
        for (TimeFrameStorage timeFrameStorage : btcEth) {
            TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
            List<PoloniexTradingRecord> tradingRecords = timeFrameStorage.getAllTradingRecords();

            StringBuilder sb = new StringBuilder("name,id,time,index,price,amount,type\n");
            tradingRecords.forEach(tradingRecord -> {
                String name = tradingRecord.getStrategyName() + "-tr-" + tradingRecord.getId();
                tradingRecord.getOrders().forEach(poloniexOrder -> sb.append(exportHelper.convertOrder(name, poloniexOrder)).append("\n"));
            });

            csvFileWriter.write("orders(" + timeFrame.getDisplayName() + ")", sb);
        }
    }

    @PreDestroy
    public void preDestroy() {
        exportData();
    }
}
