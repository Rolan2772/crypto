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
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
public class CandlesExportService implements MemoryExportService<TimeFrameStorage> {

    public static final String CANDLES_FILE_NAME = "candles-";

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
        TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
        List<PoloniexStrategy> poloniexStrategies = timeFrameStorage.getActiveStrategies();
        List<PoloniexStrategy> strategiesCopy = exportHelper.createTradingRecordsCopy(poloniexStrategies);
        List<PoloniexTradingRecord> tradingRecords = timeFrameStorage.getAllTradingRecords();
        StringBuilder sb = new StringBuilder("timePeriod,beginTime,endTime,openPrice,closePrice,maxPrice,minPrice,amount,volume")
                .append(",")
                .append(exportHelper.createStrategiesHeaders(tradingRecords, "sim"))
                .append(",")
                .append(exportHelper.createStrategiesHeaders(tradingRecords, "real"))
                .append("\n");

        int count = timeFrameStorage.getCandles().size();
        TimeSeries timeSeries = new TimeSeries(timeFrame.name(), timeFrameStorage.getCandles());
        IntStream.range(0, count).forEach(index -> sb.append(exportHelper.convertCandle(timeSeries, index))
                .append(",")
                .append(exportHelper.createHistoryTradesAnalytics(strategiesCopy, timeSeries, index, timeFrameStorage.getHistoryIndex()))
                .append(",")
                .append(exportHelper.convertRealTrades(tradingRecords, index))
                .append("\n"));

        sb.append('\n');
        sb.append("History analytics: ");
        sb.append('\n');
        sb.append(exportHelper.createResultAnalytics(timeSeries, strategiesCopy));
        sb.append('\n');
        sb.append("Real trades: ");
        sb.append('\n');
        sb.append(exportHelper.createResultAnalytics(timeSeries, poloniexStrategies));
        return sb;
    }

    private String createName(TimeFrame timeFrame, CurrencyPair currencyPair) {
        return CANDLES_FILE_NAME + currencyPair + "(" + timeFrame.getDisplayName() + ")";
    }

    @PreDestroy
    public void preDestroy() {
        List<TimeFrameStorage> btcEth = candlesStorage.getData(CurrencyPair.BTC_ETH);
        exportMemoryData(CurrencyPair.BTC_ETH, btcEth);
    }
}
