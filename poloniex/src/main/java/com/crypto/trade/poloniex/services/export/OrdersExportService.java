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
public class OrdersExportService implements MemoryExportService<TimeFrameStorage> {

    public static final String ORDERS_FILE_NAME = "orders-";

    @Autowired
    private CsvFileWriter csvFileWriter;
    @Autowired
    private CandlesStorage candlesStorage;
    @Autowired
    private ExportHelper exportHelper;

    @Override
    public void exportMemoryData(CurrencyPair currencyPair, Collection<TimeFrameStorage> data, OsType osType) {
        data.forEach(timeFrameStorage -> {
            String name = osType + "-" + createName(timeFrameStorage.getTimeFrame(), currencyPair);
            csvFileWriter.write(new ExportData(currencyPair, name, convert(timeFrameStorage), osType));
        });
    }

    @Override
    public void exportMemoryData(CurrencyPair currencyPair, Collection<TimeFrameStorage> data) {
        data.forEach(timeFrameStorage -> {
            String name = createName(timeFrameStorage.getTimeFrame(), currencyPair);
            csvFileWriter.write(new ExportData(currencyPair, name, convert(timeFrameStorage)));
        });
    }

    private StringBuilder convert(TimeFrameStorage timeFrameStorage) {
        List<PoloniexTradingRecord> tradingRecords = timeFrameStorage.getAllTradingRecords();

        StringBuilder sb = new StringBuilder("name,id,tradeTime,index,price,amount,fee,amountAfterFee,type\n");
        tradingRecords.forEach(tradingRecord -> {
            String trName = tradingRecord.getStrategyName() + "-tr-" + tradingRecord.getId();
            tradingRecord.getOrders().forEach(poloniexOrder -> sb.append(exportHelper.convertOrder(trName, poloniexOrder)).append("\n"));
        });

        sb.append("\n");
        sb.append(exportHelper.convertTotalProfit(timeFrameStorage));
        return sb;
    }

    private String createName(TimeFrame timeFrame, CurrencyPair currencyPair) {
        return ORDERS_FILE_NAME + currencyPair + "(" + timeFrame.getDisplayName() + ")";
    }

    @PreDestroy
    public void preDestroy() {
        List<TimeFrameStorage> btcEth = candlesStorage.getData(CurrencyPair.BTC_ETH);
        exportMemoryData(CurrencyPair.BTC_ETH, btcEth);
    }
}
