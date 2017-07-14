package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.AnalyticsService;
import com.crypto.trade.poloniex.services.analytics.StrategiesBuilder;
import com.crypto.trade.poloniex.services.analytics.TimeFrame;
import com.crypto.trade.poloniex.storage.TickersStorage;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CandlesExportService implements ExportDataService {

    @Autowired
    private CsvFileWriter csvFileWriter;
    @Autowired
    private TickersStorage tickersStorage;
    @Autowired
    private AnalyticsService analyticsService;
    @Autowired
    private StrategiesBuilder strategiesBuilder;

    @Override
    //@Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void exportData() {
        for (TimeFrame timeFrame : TimeFrame.values()) {
            TimeSeries ethSeries = tickersStorage.getCandles("BTC_ETH", timeFrame);
            Strategy strategy = strategiesBuilder.buildShortBuyStrategy(ethSeries);
            csvFileWriter.write("candles(" + timeFrame.getDisplayName() + ")", convert(ethSeries, strategy));
        }
    }

    private StringBuilder convert(TimeSeries timeSeries, Strategy strategy) {
        StringBuilder sb = new StringBuilder("timePeriod,beginTime,endTime,openPrice,closePrice,maxPrice,minPrice,a1,a2,amount,volume\n");
        final int nbTicks = timeSeries.getTickCount();
        TradingRecord tradingRecord = new TradingRecord();
        for (int i = 0; i < nbTicks; i++) {
            Tick tick = timeSeries.getTick(i);
            sb.append(tick.getTimePeriod()).append(',')
                    .append(tick.getBeginTime().toLocalDateTime()).append(',')
                    .append(tick.getEndTime().toLocalDateTime()).append(',')
                    .append(tick.getOpenPrice()).append(',')
                    .append(tick.getClosePrice()).append(',')
                    .append(tick.getMaxPrice()).append(',')
                    .append(tick.getMinPrice()).append(',')
                    .append(analyticsService.analyzeTick(strategy, tick, i, tradingRecord)).append(',')
                    .append(tick.getAmount()).append(',')
                    .append(tick.getVolume()).append(',')
                    .append('\n');
        }

        sb.append('\n');
        sb.append("Trades: ").append(tradingRecord.getTradeCount());
        sb.append('\n');
        sb.append("Profit: ").append(new TotalProfitCriterion().calculate(timeSeries, tradingRecord));
        return sb;
    }
}
