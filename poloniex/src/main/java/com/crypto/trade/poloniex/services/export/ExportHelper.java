package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.dto.PoloniexOrder;
import com.crypto.trade.poloniex.services.analytics.AnalyticsService;
import com.crypto.trade.poloniex.services.analytics.TradingAction;
import com.crypto.trade.poloniex.storage.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.PoloniexTradingRecord;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ExportHelper {

    @Qualifier("historyAnalyticsService")
    @Autowired
    private AnalyticsService analyticsService;

    public String createStrategiesHeaders(List<PoloniexTradingRecord> tradingRecords, String type) {
        return tradingRecords.stream()
                .map(tradingRecord -> tradingRecord.getStrategyName() + "-tr-" + tradingRecord.getId() + "-" + type)
                .collect(Collectors.joining(","));
    }

    public String convertCandle(TimeSeries timeSeries, int index) {
        Tick tick = timeSeries.getTick(index);
        return Stream.of(tick.getTimePeriod(),
                tick.getBeginTime().toLocalDateTime(),
                tick.getEndTime().toLocalDateTime(),
                tick.getOpenPrice(),
                tick.getClosePrice(),
                tick.getMaxPrice(),
                tick.getMinPrice(),
                tick.getAmount(),
                tick.getVolume())
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String convertIndicators(TimeSeries timeSeries, List<Indicator<?>> indicators, int index) {
        StringBuilder sb = new StringBuilder();
        sb.append(timeSeries.getTick(index).getEndTime().toLocalDateTime().toString()).append(",")
                .append(indicators.stream()
                        .map(indicator -> indicator.getValue(index).toString())
                        .collect(Collectors.joining(",")));
        return sb.toString();
    }

    public String createHistoryTradesAnalytics(List<PoloniexStrategy> strategies, TimeSeries timeSeries, int index) {
        Tick tick = timeSeries.getTick(index);
        return strategies.stream()
                .flatMap(strategy -> strategy.getTradingRecords()
                        .stream()
                        .map(tradingRecord -> analyticsService.analyzeTick(strategy.getStrategy(), tick, index, tradingRecord.getTradingRecord())))
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String convertRealTrades(List<PoloniexTradingRecord> tradingRecords, TimeSeries timeSeries, int index) {
        return tradingRecords.stream()
                .map(tradingRecord -> tradingRecord.getOrders()
                        .stream()
                        .filter(order -> index == order.getIndex())
                        .map(PoloniexOrder::getAction)
                        .findFirst()
                        .orElse(TradingAction.NO_ACTION))
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String createResultAnalytics(TimeSeries candles, List<PoloniexStrategy> strategies) {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append("Real trades: ");
        strategies.stream()
                .map(PoloniexStrategy::getTradingRecords)
                .flatMap(Collection::stream)
                .forEach(poloniexTradingRecord -> {
                    TradingRecord tradingRecord = poloniexTradingRecord.getTradingRecord();
                    sb.append(poloniexTradingRecord.getStrategyName()).append(" trades: ").append(tradingRecord.getTradeCount()).append('\n');
                    sb.append(poloniexTradingRecord.getStrategyName()).append(" profit: ").append(new TotalProfitCriterion().calculate(candles, tradingRecord)).append('\n');
                });
        return sb.toString();

    }

    public List<PoloniexStrategy> createTradingRecordsCopy(List<PoloniexStrategy> strategies) {
        return strategies.stream()
                .map(strategy -> {
                    List<PoloniexTradingRecord> tradingRecords = strategy.getTradingRecords().stream()
                            .map(tr -> new PoloniexTradingRecord(tr.getId(), tr.getStrategyName(), new TradingRecord()))
                            .collect(Collectors.toList());
                    return new PoloniexStrategy(strategy.getName(), strategy.getStrategy(), strategy.getTimeFrame(), tradingRecords);
                }).collect(Collectors.toList());
    }
}