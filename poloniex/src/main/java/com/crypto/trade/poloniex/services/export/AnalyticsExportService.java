package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.utils.CsvFileWriter;
import com.crypto.trade.poloniex.storage.CandlesStorage;
import com.crypto.trade.poloniex.storage.analytics.AnalyticsStorage;
import com.crypto.trade.poloniex.storage.analytics.IndicatorType;
import com.crypto.trade.poloniex.storage.model.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.model.PoloniexTradingRecord;
import com.crypto.trade.poloniex.storage.model.TimeFrameStorage;
import eu.verdelhan.ta4j.BaseTimeSeries;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.TimeSeries;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
public class AnalyticsExportService implements MemoryExportService<TimeFrameStorage> {

    public static final String ANALYTICS_FILE_NAME = "analytics-";

    @Autowired
    private CsvFileWriter csvFileWriter;
    @Autowired
    private CandlesStorage candlesStorage;
    @Autowired
    private ExportHelper exportHelper;
    @Autowired
    private AnalyticsStorage analyticsStorage;

    @Override
    public void exportMemoryData(CurrencyPair currencyPair, Collection<TimeFrameStorage> data, OsType osType) {
        data.forEach(timeFrameStorage -> {
            String name = osType + "-" + createName(timeFrameStorage.getTimeFrame(), currencyPair);
            csvFileWriter.write(new ExportData(currencyPair, name, convert(currencyPair, timeFrameStorage), osType));
        });
    }

    @Override
    public void exportMemoryData(CurrencyPair currencyPair, Collection<TimeFrameStorage> data) {
        data.forEach(timeFrameStorage -> {
            String name = createName(timeFrameStorage.getTimeFrame(), currencyPair);
            csvFileWriter.write(new ExportData(currencyPair, name, convert(currencyPair, timeFrameStorage)));
        });
    }

    private StringBuilder convert(CurrencyPair currencyPair, TimeFrameStorage timeFrameStorage) {
        TimeFrame timeFrame = timeFrameStorage.getTimeFrame();
        List<PoloniexStrategy> poloniexStrategies = timeFrameStorage.getActiveStrategies();
        List<PoloniexStrategy> strategiesCopy = exportHelper.createTradingRecordsCopy(poloniexStrategies);
        List<PoloniexTradingRecord> tradingRecords = timeFrameStorage.getAllTradingRecords();
        StringBuilder sb = new StringBuilder("timestamp,close,rsi,stochK,stochD,ema5,ema90,ema100,dma90,tma90")
                .append(",")
                .append(exportHelper.createStrategiesHeaders(tradingRecords, "sim"))
                .append(",")
                .append(exportHelper.createStrategiesHeaders(tradingRecords, "real"))
                .append("\n");

        int count = timeFrameStorage.getCandles().size();
        TimeSeries timeSeries = new BaseTimeSeries(timeFrame.name(), timeFrameStorage.getCandles());
        List<Indicator<Decimal>> indicators = createIndicators(currencyPair, timeFrame);
        IntStream.range(0, count).forEach(index -> sb.append(exportHelper.convertIndicators(timeSeries, indicators, index))
                .append(",")
                .append(exportHelper.createHistoryTradesAnalytics(strategiesCopy, timeSeries, index, timeFrameStorage.getHistoryIndex()))
                .append(",")
                .append(exportHelper.convertRealTrades(tradingRecords, index))
                .append("\n"));
        sb.append('\n');
        sb.append("History profit analytics: ");
        sb.append('\n');
        sb.append(exportHelper.convertProfit(strategiesCopy));
        sb.append('\n');
        sb.append("History trades analytics: ");
        sb.append('\n');
        sb.append(exportHelper.convertTradesData(strategiesCopy));
        sb.append('\n');
        sb.append("Real trades: ");
        sb.append('\n');
        sb.append(exportHelper.convertProfit(poloniexStrategies));
        return sb;
    }

    private String createName(TimeFrame timeFrame, CurrencyPair currencyPair) {
        return ANALYTICS_FILE_NAME + currencyPair + "(" + timeFrame.getDisplayName() + ")";
    }

    private List<Indicator<Decimal>> createIndicators(CurrencyPair currencyPair, TimeFrame timeFrame) {
        return Stream.of(analyticsStorage.<Indicator<Decimal>>getIndicator(currencyPair, timeFrame, IndicatorType.CLOSED_PRICE),
                analyticsStorage.getIndicator(currencyPair, timeFrame,
                        IndicatorType.RSI14),
                analyticsStorage.getIndicator(currencyPair, timeFrame,
                        IndicatorType.STOCHK14),
                analyticsStorage.getIndicator(currencyPair, timeFrame,
                        IndicatorType.STOCHD3),
                analyticsStorage.getIndicator(currencyPair, timeFrame,
                        IndicatorType.EMA5),
                analyticsStorage.getIndicator(currencyPair, timeFrame,
                        IndicatorType.EMA90),
                analyticsStorage.getIndicator(currencyPair, timeFrame,
                        IndicatorType.EMA100),
                analyticsStorage.getIndicator(currencyPair, timeFrame,
                        IndicatorType.DMA90),
                analyticsStorage.getIndicator(currencyPair, timeFrame,
                        IndicatorType.TMA90))
                .collect(Collectors.toList());
    }

    @PreDestroy
    public void preDestroy() {
        List<TimeFrameStorage> btcEth = candlesStorage.getData(CurrencyPair.BTC_ETH);
        exportMemoryData(CurrencyPair.BTC_ETH, btcEth);
    }
}
