package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.services.analytics.TradeStrategyFactory;
import com.crypto.trade.poloniex.services.utils.CsvFileWriter;
import com.crypto.trade.poloniex.storage.CandlesStorage;
import com.crypto.trade.poloniex.storage.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.PoloniexTradingRecord;
import com.crypto.trade.poloniex.storage.TimeFrameStorage;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorDIndicator;
import eu.verdelhan.ta4j.indicators.oscillators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.*;
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
        StringBuilder sb = new StringBuilder("timestamp,close,rsi,stochK,stochD,sma,ema5,ema90,ema100,dma90,tma90")
                .append(",")
                .append(exportHelper.createStrategiesHeaders(tradingRecords, "sim"))
                //.append(",")
                //.append(exportHelper.createStrategiesHeaders(tradingRecords, "real"))
                .append("\n");

        int count = timeFrameStorage.getCandles().size();
        TimeSeries timeSeries = new TimeSeries(timeFrame.name(), timeFrameStorage.getCandles());
        List<Indicator<?>> indicators = createIndicators(TradeStrategyFactory.DEFAULT_TIME_FRAME, timeSeries);
        IntStream.range(0, count).forEach(index -> sb.append(exportHelper.convertIndicators(timeSeries, indicators, index))
                .append(",")
                .append(exportHelper.createHistoryTradesAnalytics(strategiesCopy, timeSeries, index, timeFrameStorage.getHistoryIndex()))
                //.append(",")
                //.append(exportHelper.convertRealTrades(tradingRecords, index))
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

    private List<Indicator<?>> createIndicators(int indicatorTimeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = new RSIIndicator(closePrice, indicatorTimeFrame);
        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(timeSeries, indicatorTimeFrame);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
        SMAIndicator sma = new SMAIndicator(stochK, indicatorTimeFrame);
        EMAIndicator ema5 = new EMAIndicator(closePrice, 5);
        EMAIndicator ema90 = new EMAIndicator(closePrice, 90);
        EMAIndicator ema100 = new EMAIndicator(closePrice, 100);
        DoubleEMAIndicator dma90 = new DoubleEMAIndicator(closePrice, 90);
        TripleEMAIndicator tma90 = new TripleEMAIndicator(closePrice, 90);
        return Stream.of(closePrice, rsi, stochK, stochD, sma, ema5, ema90, dma90, tma90).collect(Collectors.toList());
    }

    @PreDestroy
    public void preDestroy() {
        List<TimeFrameStorage> btcEth = candlesStorage.getData(CurrencyPair.BTC_ETH);
        exportMemoryData(CurrencyPair.BTC_ETH, btcEth);
    }
}
