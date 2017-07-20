package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.utils.CsvFileWriter;
import com.crypto.trade.poloniex.storage.CandlesStorage;
import com.crypto.trade.poloniex.storage.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.PoloniexTradingRecord;
import com.crypto.trade.poloniex.storage.TimeFrameStorage;
import eu.verdelhan.ta4j.TimeSeries;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
public class CandlesExportService implements ExportDataService {

    @Autowired
    private CsvFileWriter csvFileWriter;
    @Autowired
    private CandlesStorage candlesStorage;
    @Autowired
    private ExportHelper exportHelper;

    @Override
    public void exportData() {
        List<TimeFrameStorage> btcEth = candlesStorage.getCandles().getOrDefault(CurrencyPair.BTC_ETH, Collections.emptyList());
        for (TimeFrameStorage timeFrameStorage : btcEth) {
            TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
            List<PoloniexStrategy> poloniexStrategies = timeFrameStorage.getActiveStrategies();
            List<PoloniexStrategy> strategiesCopy = exportHelper.createTradingRecordsCopy(poloniexStrategies);
            List<PoloniexTradingRecord> realRecords = timeFrameStorage.getAllTradingRecords();
            StringBuilder sb = new StringBuilder("timePeriod,beginTime,endTime,openPrice,closePrice,maxPrice,minPrice,amount,volume")
                    .append(",")
                    .append(exportHelper.createStrategiesHeaders(realRecords, "sim"))
                    .append(",")
                    .append(exportHelper.createStrategiesHeaders(realRecords, "real"))
                    .append("\n");

            int count = timeFrameStorage.getCandles().size();
            TimeSeries timeSeries = new TimeSeries(timeFrame.name(), timeFrameStorage.getCandles());
            IntStream.range(0, count).forEach(index -> sb.append(exportHelper.convertCandle(timeSeries, index))
                    .append(",")
                    .append(exportHelper.createHistoryTradesAnalytics(strategiesCopy, timeSeries, index))
                    .append(",")
                    .append(exportHelper.convertRealTrades(realRecords, timeSeries, index))
                    .append("\n"));

            sb.append(exportHelper.createResultAnalytics(timeSeries, poloniexStrategies));
            sb.append("\n");
            sb.append(exportHelper.createResultAnalytics(timeSeries, strategiesCopy));

            csvFileWriter.write("candles(" + timeFrame.getDisplayName() + ")", sb);
        }
    }

    @PreDestroy
    public void preDestroy() {
        exportData();
    }
}
