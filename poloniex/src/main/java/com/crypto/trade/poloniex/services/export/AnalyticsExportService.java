package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.*;
import com.crypto.trade.poloniex.services.analytics.indicators.CachedDoubleEMAIndicator;
import com.crypto.trade.poloniex.services.analytics.indicators.CachedTripleEMAIndicator;
import com.crypto.trade.poloniex.services.utils.CsvFileWriter;
import com.crypto.trade.poloniex.storage.CandlesStorage;
import com.crypto.trade.poloniex.storage.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.PoloniexTradingRecord;
import com.crypto.trade.poloniex.storage.TimeFrameStorage;
import eu.verdelhan.ta4j.BaseTimeSeries;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.EMAIndicator;
import eu.verdelhan.ta4j.indicators.RSIIndicator;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
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
    private AnalyticsCache analyticsCache;
    @Autowired
    private IndicatorFactory indicatorFactory;

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
        StringBuilder sb = new StringBuilder("timestamp,close,rsi,stochK,stochD,ema5,ema90,ema100,dma90,tma90")
                .append(",")
                .append(exportHelper.createStrategiesHeaders(tradingRecords, "sim"))
                .append(",")
                .append(exportHelper.createStrategiesHeaders(tradingRecords, "real"))
                .append("\n");

        int count = timeFrameStorage.getCandles().size();
        TimeSeries timeSeries = new BaseTimeSeries(timeFrame.name(), timeFrameStorage.getCandles());
        List<Indicator<Decimal>> indicators = createIndicators(timeFrame, timeSeries);
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

    private List<Indicator<Decimal>> createIndicators(TimeFrame timeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = analyticsCache.getIndicator(timeFrame,
                IndicatorType.RSI14,
                indicatorFactory.createRsi14Indicator(closePrice));
        StochasticOscillatorKIndicator stochK = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHK14,
                indicatorFactory.createStochK14(timeSeries));
        StochasticOscillatorDIndicator stochD = analyticsCache.getIndicator(timeFrame,
                IndicatorType.STOCHD3,
                indicatorFactory.createStochD3(stochK));
        EMAIndicator ema5 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA5,
                indicatorFactory.createEma5Indicator(closePrice));
        EMAIndicator ema90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA90,
                indicatorFactory.createEma90Indicator(closePrice));
        EMAIndicator ema100 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA100,
                indicatorFactory.createEma100Indicator(closePrice));
        EMAIndicator emaEma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA_EMA90,
                indicatorFactory.createEmaEma90Indicator(ema90));
        CachedDoubleEMAIndicator dma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.DMA90,
                indicatorFactory.createDma90Indicator(closePrice, ema90, emaEma90));
        EMAIndicator emaEmaEma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.EMA_EMA_EMA90,
                indicatorFactory.createEmaEmaEma90Indicator(emaEma90));
        CachedTripleEMAIndicator tma90 = analyticsCache.getIndicator(timeFrame,
                IndicatorType.TMA90,
                indicatorFactory.createTma90Indicator(closePrice, ema90, emaEma90, emaEmaEma90));
        return Stream.of(closePrice, rsi, stochK, stochD, ema5, ema90, ema100, dma90, tma90).collect(Collectors.toList());
    }

    @PreDestroy
    public void preDestroy() {
        List<TimeFrameStorage> btcEth = candlesStorage.getData(CurrencyPair.BTC_ETH);
        exportMemoryData(CurrencyPair.BTC_ETH, btcEth);
    }
}
