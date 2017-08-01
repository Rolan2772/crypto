package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.AnalyticsService;
import com.crypto.trade.poloniex.services.analytics.TradingAction;
import com.crypto.trade.poloniex.services.trade.TradeCalculator;
import com.crypto.trade.poloniex.services.utils.DateTimeUtils;
import com.crypto.trade.poloniex.storage.PoloniexOrder;
import com.crypto.trade.poloniex.storage.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.PoloniexTradingRecord;
import com.crypto.trade.poloniex.storage.TimeFrameStorage;
import eu.verdelhan.ta4j.*;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public class ExportHelper {

    @Qualifier("historyAnalyticsService")
    @Autowired
    private AnalyticsService analyticsService;
    @Autowired
    private TradeCalculator tradeCalculator;

    public String createStrategiesHeaders(List<PoloniexTradingRecord> tradingRecords, String type) {
        return tradingRecords.stream()
                .map(tradingRecord -> tradingRecord.getStrategyName() + "-tr-" + tradingRecord.getId() + "-" + type)
                .collect(Collectors.joining(","));
    }

    public String convertCandle(TimeSeries timeSeries, int index) {
        Tick tick = timeSeries.getTick(index);
        return Stream.of(tick.getTimePeriod(),
                DateTimeUtils.format(tick.getBeginTime()),
                DateTimeUtils.format(tick.getEndTime()),
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
        String closeTime = DateTimeUtils.format(timeSeries.getTick(index).getBeginTime());
        String values = indicators.stream()
                .map(indicator -> indicator.getValue(index))
                .map(Object::toString)
                .collect(Collectors.joining(","));
        return closeTime + "," + values;
    }

    public String createHistoryTradesAnalytics(List<PoloniexStrategy> strategies, TimeSeries timeSeries, int index, int historyIndex) {
        Tick tick = timeSeries.getTick(index);
        return strategies.stream()
                .flatMap(strategy -> strategy.getTradingRecords()
                        .stream()
                        .map(tradingRecord -> analyticsService.analyzeTick(strategy.getStrategy(),
                                tick,
                                index,
                                historyIndex,
                                false,
                                tradingRecord.getTradingRecord())))
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String convertRealTrades(List<PoloniexTradingRecord> tradingRecords, int index) {
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
        strategies.stream()
                .map(PoloniexStrategy::getTradingRecords)
                .flatMap(Collection::stream)
                .forEach(record -> {
                    TradingRecord tr = record.getTradingRecord();
                    sb.append(record.getStrategyName())
                            .append("-")
                            .append(record.getId())
                            .append(" trades: ")
                            .append(",")
                            .append(tr.getTradeCount()).append('\n');
                    sb.append(record.getStrategyName())
                            .append("-")
                            .append(record.getId())
                            .append(" profit: ")
                            .append(",")
                            .append(new TotalProfitCriterion().calculate(candles, tr)).append('\n');
                });
        return sb.toString();

    }

    public List<PoloniexStrategy> createTradingRecordsCopy(List<PoloniexStrategy> strategies) {
        return strategies.stream()
                .map(strategy -> {
                    List<PoloniexTradingRecord> tradingRecords = strategy.getTradingRecords().stream()
                            .map(tr -> new PoloniexTradingRecord(tr.getId(), tr.getStrategyName(), new TradingRecord()))
                            .collect(Collectors.toList());
                    return new PoloniexStrategy(strategy, tradingRecords);
                }).collect(Collectors.toList());
    }

    public String convertOrder(String name, PoloniexOrder poloniexOrder) {
        Order sourceOrder = poloniexOrder.getSourceOrder();
        return Stream.of(name,
                poloniexOrder.getOrderId(),
                DateTimeUtils.format(poloniexOrder.getTradeTime()),
                poloniexOrder.getIndex(),
                sourceOrder.getPrice(),
                sourceOrder.getAmount(),
                poloniexOrder.getFee(),
                tradeCalculator.getAmountAfterFee(sourceOrder),
                sourceOrder.getType())
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String convertTotalProfit(TimeFrameStorage timeFrameStorage) {
        StringBuilder sb = new StringBuilder();

        timeFrameStorage.getAllTradingRecords()
                .stream()
                .flatMap(tradingRecord -> {
                    Stream<BigDecimal> profit = Stream.of(BigDecimal.ZERO);
                    List<PoloniexOrder> orders = tradingRecord.getOrders();
                    if (!orders.isEmpty()) {
                        profit = IntStream.range(0, orders.size()).mapToObj(index -> {
                            BigDecimal trProfit = BigDecimal.ZERO;
                            PoloniexOrder order = orders.get(index);
                            if (order.getSourceOrder().getType() == Order.OrderType.SELL) {
                                PoloniexOrder buyOrder = orders.get(index - 1);
                                //trProfit =
                            }
                            return trProfit;
                        });
                    }
                    return profit;
                }).reduce(BigDecimal.ZERO, BigDecimal::add);
        sb.append("Total profit: ,");
        return sb.toString();
    }
}