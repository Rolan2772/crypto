package com.crypto.trade.poloniex.services.export;

import com.crypto.trade.poloniex.services.analytics.AnalyticsService;
import com.crypto.trade.poloniex.services.analytics.TradingAction;
import com.crypto.trade.poloniex.services.trade.Profit;
import com.crypto.trade.poloniex.services.trade.TradeCalculator;
import com.crypto.trade.poloniex.services.utils.CalculationsUtils;
import com.crypto.trade.poloniex.services.utils.DateTimeUtils;
import com.crypto.trade.poloniex.services.utils.ExportUtils;
import com.crypto.trade.poloniex.storage.PoloniexOrder;
import com.crypto.trade.poloniex.storage.PoloniexStrategy;
import com.crypto.trade.poloniex.storage.PoloniexTradingRecord;
import com.crypto.trade.poloniex.storage.TimeFrameStorage;
import eu.verdelhan.ta4j.*;
import eu.verdelhan.ta4j.analysis.criteria.LinearTransactionCostCriterion;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ExportHelper {

    @Qualifier("historyAnalyticsService")
    @Autowired
    private AnalyticsService analyticsService;

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
        StringBuilder sb = new StringBuilder("name,tradesCount,profit,LinearTransactionCostCriterion\n");
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
                                .append(",")
                                .append(new LinearTransactionCostCriterion(entry.getKey().doubleValue(), CalculationsUtils.FEE_PERCENT.doubleValue()).calculate(candles, tr))
                                .append('\n');
                    });
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
                sourceOrder.isBuy() ? TradeCalculator.getAmountWithFee(sourceOrder) : "",
                sourceOrder.isBuy() ? "" : TradeCalculator.getTotal(sourceOrder),
                sourceOrder.isBuy() ? TradeCalculator.getTotal(sourceOrder) : TradeCalculator.getTotalWithFee(sourceOrder),
                sourceOrder.getType())
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String convertTotalProfit(TimeFrameStorage timeFrameStorage) {
        Profit totalProfit = timeFrameStorage.getActiveStrategies().stream()
                .flatMap(strategy -> strategy.getTradingRecords()
                        .stream()
                        .map(tradingRecord -> getTradingRecordProfit(tradingRecord, strategy.getTradeVolume())))
                .reduce(new Profit(), profitAccumulator());
        return convertProfit("Total", totalProfit);
    }

    public String convertStrategyProfit(PoloniexStrategy strategy) {
        Profit strategyProfit = strategy.getTradingRecords()
                .stream()
                .map(tradingRecord -> getTradingRecordProfit(tradingRecord, strategy.getTradeVolume()))
                .reduce(new Profit(), profitAccumulator());
        return convertProfit(strategy.getName(), strategyProfit);
    }

    public String convertTradingRecordProfit(PoloniexTradingRecord tradingRecord, BigDecimal volume) {
        String name = ExportUtils.getTradingRecordName(tradingRecord);
        Profit profit = getTradingRecordProfit(tradingRecord, volume);
        return convertProfit(name, profit);
    }

    private Profit getTradingRecordProfit(PoloniexTradingRecord tradingRecord, BigDecimal volume) {
        Profit profit = new Profit();
        profit.accumulateVolume(volume);
        List<PoloniexOrder> orders = tradingRecord.getOrders();
        if (orders.size() > 1) {
            for (int index = 1; index < orders.size(); index += 2) {
                Order entryOrder = orders.get(index - 1).getSourceOrder();
                Order exitOrder = orders.get(index).getSourceOrder();
                profit.accumulate(TradeCalculator.getBuySpent(entryOrder),
                        TradeCalculator.getNetSellGain(exitOrder),
                        TradeCalculator.getGrossSellGain(entryOrder, exitOrder));
            }
        }
        return profit;
    }

    private BinaryOperator<Profit> profitAccumulator() {
        return (profit1, profit2) -> {
            profit1.accumulate(profit2.getBuySpent(),
                    profit2.getNetSellGain(),
                    profit2.getGrossSellGain());
            profit1.accumulateVolume(profit2.getVolume());
            return profit1;
        };
    }

    private String convertProfit(String name, Profit profit) {
        return name + "," +
                profit.getGrossProfit() +
                "," +
                profit.getNetProfit() +
                "," +
                profit.getGrossPercent() +
                "," +
                profit.getNetPercent();
    }
}