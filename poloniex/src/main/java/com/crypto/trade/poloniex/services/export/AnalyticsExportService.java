package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.AnalyticsService;
import com.crypto.trade.poloniex.services.analytics.CurrencyPair;
import com.crypto.trade.poloniex.services.analytics.StrategiesBuilder;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
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
import eu.verdelhan.ta4j.indicators.trackers.EMAIndicator;
import eu.verdelhan.ta4j.indicators.trackers.RSIIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
public class AnalyticsExportService implements ExportDataService {

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
            List<PoloniexStrategy> poloniexStrategies = timeFrameStorage.getActiveStrategies();
            List<PoloniexStrategy> strategiesCopy = exportHelper.createTradingRecordsCopy(poloniexStrategies);
            List<PoloniexTradingRecord> tradingRecords = timeFrameStorage.getAllTradingRecords();
            StringBuilder sb = new StringBuilder("timestamp,close,rsi,stochK,stochD,sma,ema32,ema128")
                    .append(",")
                    .append(exportHelper.createStrategiesHeaders(tradingRecords, "sim"))
                    .append(",")
                    .append(exportHelper.createStrategiesHeaders(tradingRecords, "real"))
                    .append("\n");

            int count = timeFrameStorage.getCandles().size();
            TimeSeries timeSeries = new TimeSeries(timeFrame.name(), timeFrameStorage.getCandles());
            List<Indicator<?>> indicators = createIndicators(indicatorTimeFrame, timeSeries);
            IntStream.range(0, count).forEach(index -> sb.append(exportHelper.convertIndicators(timeSeries, indicators, index))
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

            csvFileWriter.write("analytics(" + timeFrame.getDisplayName() + ")", sb);
        }
    }

    private List<Indicator<?>> createIndicators(int indicatorTimeFrame, TimeSeries timeSeries) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        RSIIndicator rsi = new RSIIndicator(closePrice, indicatorTimeFrame);
        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(timeSeries, indicatorTimeFrame);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
        SMAIndicator sma = new SMAIndicator(stochK, indicatorTimeFrame);
        EMAIndicator ema32 = new EMAIndicator(closePrice, 32);
        EMAIndicator ema128 = new EMAIndicator(closePrice, 128);
        return Stream.of(closePrice, rsi, stochK, stochD, sma, ema32, ema128).collect(Collectors.toList());
    }

    @PreDestroy
    public void preDestroy() {
        exportData();
    }
}
