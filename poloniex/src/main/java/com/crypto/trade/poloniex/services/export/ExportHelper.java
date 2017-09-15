package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.AnalyticsService;
import com.crypto.trade.poloniex.services.analytics.TradingAction;
import com.crypto.trade.poloniex.services.trade.ProfitCalculator;
import com.crypto.trade.poloniex.services.trade.TradeCalculator;
import com.crypto.trade.poloniex.services.trade.TradeResult;
import com.crypto.trade.poloniex.services.utils.DateTimeUtils;
import com.crypto.trade.poloniex.services.utils.ExportUtils;
import com.crypto.trade.poloniex.storage.PoloniexOrder;
import com.crypto.trade.poloniex.storage.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.PoloniexTradingRecord;
import eu.verdelhan.ta4j.*;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ExportHelper {

    @Qualifier("historyAnalyticsService")
    @Autowired
    private AnalyticsService analyticsService;
    @Autowired
    private ProfitCalculator profitCalculator;

    public String createStrategiesHeaders(List<PoloniexTradingRecord> tradingRecords, String type) {
        return tradingRecords.stream()
                .map(tradingRecord -> ExportUtils.getTradingRecordName(tradingRecord) + "-" + type)
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

    public String convertIndicators(TimeSeries timeSeries, List<Indicator<Decimal>> indicators, int index) {
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
                .flatMap(strategy -> {
                    BigDecimal volume = strategy.getTradeVolume();
                    return strategy.getTradingRecords()
                            .stream()
                            .map(tradingRecord -> analyticsService.analyzeTick(strategy.getStrategy(),
                                    tick,
                                    index,
                                    historyIndex,
                                    false,
                                    tradingRecord.getTradingRecord(),
                                    strategy.getDirection(),
                                    volume));
                })
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
        StringBuilder sb = new StringBuilder("name,tradesCount,profit\n");
        strategies.stream()
                .map(strategy -> new AbstractMap.SimpleEntry<>(strategy.getTradeVolume(), strategy.getTradingRecords()))
                .forEach(entry -> {
                    entry.getValue().forEach(record -> {
                        TradingRecord tr = record.getTradingRecord();
                        sb.append(ExportUtils.getTradingRecordName(record))
                                .append(",")
                                .append(tr.getTradeCount())
                                .append(",")
                                .append(new TotalProfitCriterion().calculate(candles, tr))
                                .append('\n');
                    });
                });
        return sb.toString();

    }

    public List<PoloniexStrategy> createTradingRecordsCopy(List<PoloniexStrategy> strategies) {
        return strategies.stream()
                .map(strategy -> {
                    List<PoloniexTradingRecord> tradingRecords = strategy.getTradingRecords().stream()
                            .map(tr -> new PoloniexTradingRecord(tr.getId(), tr.getStrategyName(), strategy.getDirection()))
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
                sourceOrder.isBuy() ? TradeCalculator.getExitAmount(sourceOrder, BigDecimal.ONE) : "",
                sourceOrder.isBuy() ? "" : TradeCalculator.getTotal(sourceOrder),
                sourceOrder.isBuy() ? TradeCalculator.getTotal(sourceOrder) : TradeCalculator.getTotalWithFee(sourceOrder),
                sourceOrder.getType())
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String convertProfit(List<PoloniexStrategy> strategies) {
        StringBuilder profit = new StringBuilder("description,tradesCount,volume,grossProfit,netProfit,grossProfit%,netProfit%\n");
        strategies.stream()
                .collect(Collectors.groupingBy(PoloniexStrategy::getDirection))
                .entrySet()
                .forEach(entry -> {
                    Order.OrderType direction = entry.getKey();
                    entry.getValue().forEach(poloniexStrategy -> {
                        profit.append(poloniexStrategy.getTradingRecords()
                                .stream()
                                .map(tradingRecord -> convertTradingRecordProfit(tradingRecord,
                                        poloniexStrategy.getDirection(),
                                        poloniexStrategy.getTradeVolume()))
                                .collect(Collectors.joining("")));
                        profit.append(convertStrategyProfit(poloniexStrategy));
                    });
                    profit.append(convertTotalProfit(strategies, direction));
                });
        return profit.toString();
    }

    public String convertTotalProfit(List<PoloniexStrategy> strategies, Order.OrderType direction) {
        TradeResult totalTradeResult = profitCalculator.getTotalTradeResult(strategies, direction);
        return convertProfit("Total " + direction, totalTradeResult);
    }

    public String convertStrategyProfit(PoloniexStrategy strategy) {
        TradeResult strategyTradeResult = profitCalculator.getStrategyTradeResult(strategy);
        return convertProfit(strategy.getName(), strategyTradeResult);
    }

    public String convertTradingRecordProfit(PoloniexTradingRecord tradingRecord, Order.OrderType direction, BigDecimal volume) {
        String name = ExportUtils.getTradingRecordName(tradingRecord);
        TradeResult tradeResult = profitCalculator.getTradingRecordProfit(tradingRecord, direction, volume);
        return convertProfit(name, tradeResult);
    }

    private String convertProfit(String name, TradeResult tradeResult) {
        String converted = Stream.of(name,
                tradeResult.getTradesCount(),
                tradeResult.getVolume(),
                profitCalculator.getGrossProfit(tradeResult),
                profitCalculator.getNetProfit(tradeResult),
                profitCalculator.getGrossPercent(tradeResult),
                profitCalculator.getNetPercent(tradeResult))
                .map(Object::toString)
                .collect(Collectors.joining(","));
        return converted + "\n";
    }

    public String convertTradesData(List<PoloniexStrategy> strategies) {
        StringBuilder trades = new StringBuilder("description,intervals\n");
        strategies.stream()
                .collect(Collectors.groupingBy(PoloniexStrategy::getDirection))
                .entrySet()
                .forEach(entry -> {
                    trades.append(entry.getKey()).append("\n");
                    trades.append(entry.getValue()
                            .stream()
                            .map(poloniexStrategy ->
                                    poloniexStrategy.getName() + "," + poloniexStrategy.getTradingRecords()
                                            .stream()
                                            .map(tradesIntervalMapper())
                                            .flatMap(Collection::stream)
                                            .map(TradeInterval::getReportView)
                                            .collect(Collectors.joining(",")))
                            .collect(Collectors.joining("\n")));
                });
        return trades.toString();
    }

    private Function<PoloniexTradingRecord, List<TradeInterval>> tradesIntervalMapper() {
        return tradingRecord -> tradingRecord.getTradingRecord().getTrades().stream()
                .filter(Trade::isClosed)
                .map(TradeInterval::new)
                .collect(Collectors.toList());
    }
}